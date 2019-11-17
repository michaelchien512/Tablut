# Tablut
Tablut is one of the family of tafl (or hnefatafl) games: Nordic and Celtic strategy games played on checkered boards between two asymmetric armies. The detailed rules of these games are generally disputed, 
therefore this version of Tablut may have rules you are unfamiliar with. This version of Tablut comes with an automated AI implemented using
a game tree with the miniMax algorithm with alpha-beta pruning and is capable of finding a forced win. (Can still be improved)
## Rules
Tablut is played on a 9x9 checkerboard starting with 9 white pieces and 16 black pieces.The middle square is called the throne where the king
resides. The other white pieces are known as *Swedes* and are the King's guards. The white side wins if the King reaches any of the edges of the board.
The black pieces or *Muscovites* are trying to capture the King before the King can reach the edge of the board. 

![Annotation 2019-11-17 021859](https://user-images.githubusercontent.com/47373165/69006267-6564f380-08e1-11ea-852b-92123e0b3758.png?style=centerme)

All pieces move like chess rooks : moving only in the orthogonal directions. Pieces may not jump over each other or land on each other. No 
other piece but the King may go into the throne square. A piece other than the King is captured when as a result of **an enemy move to the adjacent square**
the piece is enclosed orthonogally by hostile squares. A *square* is considered hostile if an enemy piece occupies the square or if the throne square is 
empty in which in that case the throne is hostile to both white and black. A capture can only happen as a result of an enemy move. 
The throne occupied by the King is also hostile to white pieces when three of the four squares surrounding the throne are occupied by black pieces. \

![w5GSBMfEh1](https://user-images.githubusercontent.com/47373165/69006659-02765b00-08e7-11ea-9a53-80aa7394b713.gif)

*Ex: The white piece captured on **"g-5"** is the result of **"i4-g", "f5-2", "i6-g**.*

The King is captured like any other piece except when he is on the throne or on one of the four squares directly orthogonal to the throne
in which he must be surrouded by four hostile squares. The game can also be won if a board position is repeated or a side has no legal moves left to play. 

