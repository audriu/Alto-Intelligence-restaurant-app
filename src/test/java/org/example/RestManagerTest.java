package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

class RestManagerTest {
    private RestManager restManager;
    private List<Table> tables;

    @BeforeEach
    void setUp() {
        tables = new ArrayList<>();
        tables.add(new Table(2));
        tables.add(new Table(3));
        tables.add(new Table(4));
        tables.add(new Table(5));
        tables.add(new Table(6));
        restManager = new RestManager(tables);
    }

    @Test
    void testOnArrive_SingleClient() {
        ClientsGroup group = new ClientsGroup(1);
        restManager.onArrive(group);
        assertEquals(2, restManager.lookup(group).size);
    }

    @Test
    void testOnArrive_GroupLargerThanAvailableTables() {
        ClientsGroup group = new ClientsGroup(7);
        restManager.onArrive(group);
        assertNull(restManager.lookup(group));
    }

    @Test
    void testOnArrive_MultipleGroups() {
        ClientsGroup group1 = new ClientsGroup(2);
        ClientsGroup group2 = new ClientsGroup(4);
        ClientsGroup group3 = new ClientsGroup(3);
        restManager.onArrive(group1);
        restManager.onArrive(group2);
        restManager.onArrive(group3);
        assertEquals(2, restManager.lookup(group1).size);
        assertEquals(4, restManager.lookup(group2).size);
        assertEquals(3, restManager.lookup(group3).size);
    }

    @Test
    void testOnLeave_GroupLeavesAndNewGroupIsSeated() {
        ClientsGroup group1 = new ClientsGroup(2);
        ClientsGroup group2 = new ClientsGroup(4);
        restManager.onArrive(group1);
        restManager.onArrive(group2);
        restManager.onLeave(group1);
        ClientsGroup group3 = new ClientsGroup(3);
        restManager.onArrive(group3);
        assertNull(restManager.lookup(group1));
        assertEquals(4, restManager.lookup(group2).size);
        assertEquals(3, restManager.lookup(group3).size);
    }

    @Test
    void testWaitIfNoSpace() {
        ClientsGroup group1 = new ClientsGroup(6);
        ClientsGroup group2 = new ClientsGroup(6);
        restManager.onArrive(group1);
        restManager.onArrive(group2);
        assertNotNull(restManager.lookup(group1));
        assertNull(restManager.lookup(group2));
    }

    @Test
    void testUseEmptyTablesFirst() {
        ClientsGroup group2 = new ClientsGroup(2);
        ClientsGroup group3 = new ClientsGroup(3);
        ClientsGroup group4 = new ClientsGroup(4);
        ClientsGroup group5 = new ClientsGroup(5);
        ClientsGroup group6 = new ClientsGroup(6);

        tables = new ArrayList<>();
        tables.add(new Table(3));
        tables.add(new Table(6));
        restManager = new RestManager(tables);

        //Fill all tables
        restManager.onArrive(group3);
        restManager.onArrive(group4);
        restManager.onArrive(group2);

        //All tables should be used
        assertEquals(3, restManager.lookup(group3).size);
        assertEquals(6, restManager.lookup(group4).size);
        assertEquals(6, restManager.lookup(group2).size);

        //Now group of 4 and group of 2 leave
        restManager.onLeave(group3);
        restManager.onLeave(group2);
        // this makes table of size 3 empty and table of size 6 have 2 seats.

        //now group of 2 comes, and it should be seated at table of size 3 even if it is too big
        // and not into table 6 with 2 places even of number of seats matches
        restManager.onArrive(group2);
        assertEquals(3, restManager.lookup(group2).size);
    }
}