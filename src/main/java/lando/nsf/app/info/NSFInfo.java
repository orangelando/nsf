package lando.nsf.app.info;

import java.util.Objects;
import java.util.Set;

import lando.nsf.NtscPalMode;
import lando.nsf.SoundChip;

public final class NSFInfo {

    public final int versionNumber;
    public final int totalSongs;
    public final int startingSong;
    public final int loadDataAddr;
    public final int initDataAddr;
    public final int playDataAddr;
    public final String songName;
    public final String artistName;
    public final String copyrightHolder;
    public final int ntscSpeed;
    public final int[] bankswitchInitValues;
    public final int palSpeed;
    public final NtscPalMode ntscPalMode;
    public final Set<SoundChip> supportedExtraSoundChips;
    
    public NSFInfo(
            int versionNumber, 
            int totalSongs, 
            int startingSong, 
            int loadDataAddr, 
            int initDataAddr,
            int playDataAddr, 
            String songName, 
            String artistName, 
            String copyrightHolder, 
            int ntscSpeed,
            int[] bankswitchInitValues, 
            int palSpeed, 
            NtscPalMode ntscPalMode,
            Set<SoundChip> supportedExtraSoundChips) {
        
        this.versionNumber = versionNumber;
        this.totalSongs = totalSongs;
        this.startingSong = startingSong;
        this.loadDataAddr = loadDataAddr;
        this.initDataAddr = initDataAddr;
        this.playDataAddr = playDataAddr;
        this.songName = Objects.requireNonNull(songName);
        this.artistName = Objects.requireNonNull(artistName);
        this.copyrightHolder = Objects.requireNonNull(copyrightHolder);
        this.ntscSpeed = ntscSpeed;
        this.bankswitchInitValues = Objects.requireNonNull(bankswitchInitValues);
        this.palSpeed = palSpeed;
        this.ntscPalMode = Objects.requireNonNull(ntscPalMode);
        this.supportedExtraSoundChips = Objects.requireNonNull(supportedExtraSoundChips);
    }
}
