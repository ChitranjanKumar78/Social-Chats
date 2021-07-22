package com.chitranjank.apps.socialchats;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private String timeStamp;
    private String mp3File;
    private String pdfFile;


    private String imgUrl;
    private boolean isSeen;

    public Chat() {
    }

    public boolean isIsSeen() {
        return isSeen;
    }

    public Chat(String sender, String receiver, String message, String timeStamp, String imgUrl,
                boolean isSeen, String mp3File, String pdfFile) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timeStamp = timeStamp;
        this.imgUrl = imgUrl;
        this.isSeen = isSeen;
        this.mp3File = mp3File;
        this.pdfFile = pdfFile;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public String getMp3File() {
        return mp3File;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public String getImgUrl() {
        return imgUrl;
    }

}
