package tablut;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.awt.Dimension;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;


import java.util.concurrent.ArrayBlockingQueue;

/** The GUI controller for a Tablut board and buttons.
 *  @author Michael Chien
 */
class GUI extends TopLevel implements View, Reporter {

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 500;

    /** Size of pane used to contain help text. */
    static final Dimension TEXT_BOX_SIZE = new Dimension(500, 700);

    /** Resource name of "About" message. */
    static final String ABOUT_TEXT = "tablut/About.html";

    /** Resource name of Tablut help text. */
    static final String HELP_TEXT = "tablut/Help.html";

    /** A new window with given TITLE providing a view of a Tablut board. */
    GUI(String title) {
        super(title, true);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Game->New", this::newgame);
        addMenuButton("Game->Undo", this::undo);
        addMenuButton("Settings-> manual black", this::manualblack);
        addMenuButton("Settings-> manual white", this::manualwhite);
        addMenuButton("Settings-> auto black", this::autoBlack);
        addMenuButton("Settings-> auto white", this::autoWhite);
        _widget = new BoardWidget(_pendingCommands);
        add(_widget,
            new LayoutSpec("y", 1,
                           "height", 1,
                           "width", 3));
        addLabel("To move: White", "CurrentTurn",
                 new LayoutSpec("x", 0, "y", 0,
                                "height", 1,
                                "width", 3));

    }

    /** Response to "Quit" button click. */
    private void quit(String dummy) {
        _pendingCommands.offer("quit");
    }
    /** Undo a Move. */
    private void undo(String dummy) {
        _pendingCommands.offer("undo");
    }
    /** Return the next command from our widget, waiting for it as necessary.
     *  The BoardWidget uses _pendingCommands to queue up moves that it
     *  receives.  Thie class uses _pendingCommands to queue up commands that
     *  are generated by clicking on menu items. */
    String readCommand() {
        try {
            _widget.setMoveCollection(true);
            String cmnd = _pendingCommands.take();
            _widget.setMoveCollection(false);
            return cmnd;
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }
    /** @param  game Make new game. */
    private void newgame(String game) {
        _pendingCommands.offer("new");
    }

    /** @param player Make a black player manual. */
    private void manualblack(String player) {
        _pendingCommands.offer("manual black");
    }
    /** @param player Make white player manual. */
    private void manualwhite(String player) {
        _pendingCommands.offer("manual white");
    }
    /** @param ai Make black player auto */
    private void autoBlack(String ai) {
        _pendingCommands.offer("auto black");
    }
    /** @param ai Make white player auto */
    private void autoWhite(String ai) {
        _pendingCommands.offer("auto white");
    }


    @Override
    public void update(Controller controller) {
        Board board = controller.board();

        _widget.update(board);
        if (board.winner() != null) {
            setLabel("CurrentTurn",
                     String.format("Winner: %s%s",
                                   board.winner().toName(),
                                   board.repeatedPosition()
                                   ? " (repeated board)"
                                   : ""));
        } else {
            setLabel("CurrentTurn",
                     String.format("To move: %s", board.turn().toName()));
        }

    }

    /** Display text in resource named TEXTRESOURCE in a new window titled
     *  TITLE. */
    private void displayText(String title, String textResource) {
        /* Implementation note: It would have been more convenient to avoid
         * having to read the resource and simply use dispPane.setPage on the
         * resource's URL.  However, we wanted to use this application with
         * a nonstandard ClassLoader, and arranging for straight Java to
         * understand non-standard URLS that access such a ClassLoader turns
         * out to be a bit more trouble than it's worth. */
        JFrame frame = new JFrame(title);
        JEditorPane dispPane = new JEditorPane();
        dispPane.setEditable(false);
        dispPane.setContentType("text/html");
        InputStream resource =
            GUI.class.getClassLoader().getResourceAsStream(textResource);
        StringWriter text = new StringWriter();
        try {
            while (true) {
                int c = resource.read();
                if (c < 0) {
                    dispPane.setText(text.toString());
                    break;
                }
                text.write(c);
            }
        } catch (IOException e) {
            return;
        }
        JScrollPane scroller = new JScrollPane(dispPane);
        scroller.setVerticalScrollBarPolicy(scroller.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(TEXT_BOX_SIZE);
        frame.add(scroller);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void reportError(String fmt, Object... args) {
        showMessage(String.format(fmt, args), "Tablut Error", "error");
    }

    @Override
    public void reportNote(String fmt, Object... args) {
        showMessage(String.format(fmt, args), "Tablut Message", "information");
    }

    @Override
    public void reportMove(Move unused) {
    }

    /** The board widget. */
    private BoardWidget _widget;

    /** Queue of pending commands resulting from menu clicks and moves on the
     *  board.  We use a blocking queue because the responses to clicks
     *  on the board and on menus happen in parallel to the methods that
     *  call readCommand, which therefore needs to wait for clicks to happen. */
    private ArrayBlockingQueue<String> _pendingCommands =
        new ArrayBlockingQueue<>(5);

}
