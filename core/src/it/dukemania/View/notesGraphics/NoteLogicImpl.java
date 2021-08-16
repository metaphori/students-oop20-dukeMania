package it.dukemania.View.notesGraphics;


public class NoteLogicImpl implements NoteLogic {
    private int height;
    private long noteStarts;
    private long duration;
    private int id;
    private Columns column;


    public NoteLogicImpl(final int height, final long start, final int id, final Columns column, final long duration) {
        this.height = height;
        this.noteStarts = start;
        this.id = id;
        this.column = column;
        this.duration = duration;

    }

    @Override
    public Columns getColumn() {
        return this.column;
    }

    @Override
    public long getTimeStart() {
        return this.noteStarts;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }



}
