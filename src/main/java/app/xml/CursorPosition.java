package app.xml;

public class CursorPosition {

    private long row = 1;
    private long column = 1;
    private Character prevChar = null;
    private String lineSeparator = System.lineSeparator();

    public void move(char c){
        String ch = String.valueOf(c);
        if(ch.equals(lineSeparator)){
            moveToNextLine();
            return;
        }
        if(prevChar != null
                && String.valueOf(prevChar).concat(ch).equals(lineSeparator)){
            moveToNextLine();
        }
        prevChar = c;
    }

    private void moveToNextLine(){
        row++;
        column = 1;
    }

}
