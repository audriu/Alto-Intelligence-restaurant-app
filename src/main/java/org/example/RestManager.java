package org.example;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

class Table {
    public final int size; // number of chairs

    public Table(int size) {
        this.size = size;
    }
}

class ClientsGroup {
    public final int size; // number of clients

    public ClientsGroup(int size) {
        this.size = size;
    }
}

public class RestManager {
    private final List<Table> tables;
    private final Queue<ClientsGroup> waitingQueue;
    private final Map<ClientsGroup, Table> seatedGroups;

    public RestManager(List<Table> tables) {
        this.tables = tables;
        this.waitingQueue = new LinkedList<>();
        this.seatedGroups = new HashMap<>();
    }

    public void onArrive(ClientsGroup group) {
        waitingQueue.offer(group);
        assignTables();
    }

    public void onLeave(ClientsGroup group) {
        Table table = seatedGroups.remove(group);
        if (table != null) {
            assignTables();
        }
    }

    public Table lookup(ClientsGroup group) {
        return seatedGroups.get(group);
    }

    private void assignTables() {
        Iterator<ClientsGroup> iterator = waitingQueue.iterator();
        while (iterator.hasNext()) {
            ClientsGroup group = iterator.next();
            Table availableTable = findAvailableTable(group.size);
            System.out.println(group);

            if (availableTable != null) {
                iterator.remove();
                seatedGroups.put(group, availableTable);
            }
        }
    }

    private List<Table> getAllUnseatedTables() {
        List<Table> unseatedTables = new LinkedList<>(tables);
        for (ClientsGroup group : seatedGroups.keySet()) {
            unseatedTables.remove(seatedGroups.get(group));
        }
        return unseatedTables;
    }

    private Map<Table, Integer> getFreeSpaceAtTables() {
        Map<Table, Integer> freeSpaces = new HashMap<>();
        for (Table table : tables) {
            int vacantPlaces = table.size;
            for (ClientsGroup group : seatedGroups.keySet()) {
                if (seatedGroups.get(group) == table) {
                    vacantPlaces -= group.size;
                }
            }
            freeSpaces.put(table, vacantPlaces);
        }
        return freeSpaces;
    }

    private Table findAvailableTable(int size) {
        List<Table> freeTables = getAllUnseatedTables();
        if (!freeTables.isEmpty()) {
            return freeTables.stream()
                    .filter(t -> t.size >= size)
                    .min(Comparator.comparingInt(t -> t.size))
                    .orElse(null);
        }

        Map<Table, Integer> freeSpaces = getFreeSpaceAtTables();
        List<Table> suitableTables = freeSpaces.entrySet().stream()
                .filter(entry -> entry.getValue() >= size)
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparingInt(t -> t.size))
                .collect(Collectors.toList());

        return suitableTables.isEmpty() ? null : suitableTables.get(0);
    }
}
