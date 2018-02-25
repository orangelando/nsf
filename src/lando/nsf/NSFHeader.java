package lando.nsf;

public final class NSFHeader {
    public byte[] nesm = new byte[5];
    public int versionNumber;
    public int totalSongs;
    public int startingSong;
    public int loadDataAddr;
    public int initDataAddr;
    public int playDataAddr;
    public String songName;
    public String artistName;
    public String copyrightHolder;
    public int ntscSpeed;
    public int[] bankswitchInitValues = new int[8];
    public int palSpeed;
    public int palNtscBits;
    public int extraSoungChipSupport;
}

