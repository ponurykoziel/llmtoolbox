package com.sheahorn.llmtoolbox.basics.notes;

public class NoteResponse {
    public Long id;
    public String title;
    public String content;

    public static NoteResponse from(Note note) {
        NoteResponse r = new NoteResponse();
        r.id = note.id;
        r.title = note.title;
        r.content = note.content;
        return r;
    }
}
