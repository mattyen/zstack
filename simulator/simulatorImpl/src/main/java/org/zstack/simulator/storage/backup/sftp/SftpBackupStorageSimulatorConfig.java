package org.zstack.simulator.storage.backup.sftp;

import org.zstack.storage.backup.sftp.SftpBackupStorageCommands;
import org.zstack.utils.data.SizeUnit;

import java.util.ArrayList;
import java.util.List;

public class SftpBackupStorageSimulatorConfig {
    public volatile boolean connectSuccess = true;
    public volatile long totalCapacity = SizeUnit.GIGABYTE.toByte(1000);
    public volatile long usedCapacity;
    public volatile long availableCapacity = SizeUnit.GIGABYTE.toByte(1000);
    public volatile boolean downloadSuccess1 = true;
    public volatile boolean downloadSuccess2 = true;
    public volatile long imageSize = 1;
    public volatile String imageMd5sum;
    public volatile boolean deleteSuccess = true;
    public volatile boolean pingSuccess = true;
    public volatile boolean pingException = false;
    public volatile boolean getSshkeySuccess = true;
    public volatile boolean getSshkeyException = false;
    public volatile List<SftpBackupStorageCommands.DeleteCmd> deleteCmds = new ArrayList<SftpBackupStorageCommands.DeleteCmd>();

}
