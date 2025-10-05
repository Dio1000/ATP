package me.dariansandru.utils.manual;

import java.util.List;

public interface Manual {
    List<ManualEntry> getEntries();

    void buildEntries();
    ManualEntry buildEntry(String fileName);

    ManualEntry getEntryByName(String name);
}
