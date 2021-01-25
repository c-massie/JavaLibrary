package scot.massie.lib.collections.tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveTreeTest extends TreeTest<RecursiveTree<String, Integer>>
{
    @Override
    public RecursiveTree<String, Integer> getNewEmptyTree()
    { return new RecursiveTree<>(); }

    @Override @Test
    void setRootItem()
    {
        RecursiveTree<String, Integer> tree = getNewEmptyTree();
        tree.setRootItem(5);
        assertTrue(tree.hasRootItem, "Setting the root item of a new empty tree did not set .hasRootItem to true.");
        assertEquals(5, tree.rootItem, "Setting the root item of a new empty tree did not set .rootItem to that item.");

        tree.setRootItem(6);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already had one set .hasRootItem to false.");
        assertEquals(6, tree.rootItem, "Setting the root item of a tree that already had one did not update .rootItem.");

        tree.setRootItem(null);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already had one set to null .hasRootItem to false.");
        assertEquals(null, tree.rootItem, "Setting the root item  of a tree that already had one to null did not update .rootItem.");

        tree = getNewEmptyTree();
        tree.setRootItem(null);
        assertTrue(tree.hasRootItem, "Setting the root item of a new empty tree to null did not set .hasRootItem to true.");
        assertEquals(null, tree.rootItem, "Setting the root item of a new empty tree to null did not set .rootItem to null.");

        tree.setRootItem(null);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already have one set to null, to null set .hasRootItem to false.");
        assertEquals(null, tree.rootItem, "Setting the root item of a tree that already have one set to null, to null set .rootItem to something other than null.");

        tree.setRootItem(7);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already had one set to null, set .hasRootItem to false.");
        assertEquals(7, tree.rootItem, "Setting the root item of a tree that already had one set to null did not update .rootItem.");
    }

    @Override @Test
    void setAt()
    {
        RecursiveTree<String, Integer> tree = getNewEmptyTree();
        tree.setAt(5);
        assertTrue(tree.hasRootItem, "Setting the root item of a new empty tree did not set .hasRootItem to true.");
        assertEquals(5, tree.rootItem, "Setting the root item of a new empty tree did not set .rootItem to that item.");

        tree.setAt(6);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already had one set .hasRootItem to false.");
        assertEquals(6, tree.rootItem, "Setting the root item of a tree that already had one did not update .rootItem.");

        tree.setAt(null);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already had one set to null .hasRootItem to false.");
        assertEquals(null, tree.rootItem, "Setting the root item  of a tree that already had one to null did not update .rootItem.");

        tree = getNewEmptyTree();
        tree.setAt(null);
        assertTrue(tree.hasRootItem, "Setting the root item of a new empty tree to null did not set .hasRootItem to true.");
        assertEquals(null, tree.rootItem, "Setting the root item of a new empty tree to null did not set .rootItem to null.");

        tree.setAt(null);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already have one set to null, to null set .hasRootItem to false.");
        assertEquals(null, tree.rootItem, "Setting the root item of a tree that already have one set to null, to null set .rootItem to something other than null.");

        tree.setAt(7);
        assertTrue(tree.hasRootItem, "Setting the root item of a tree that already had one set to null, set .hasRootItem to false.");
        assertEquals(7, tree.rootItem, "Setting the root item of a tree that already had one set to null did not update .rootItem.");

        tree = getNewEmptyTree();
        tree.setAt(8, "doot");
        assertEquals(1, tree.branches.size(), "Setting an item at a level 1 path on a new empty tree did not create a branch.");
        assertTrue(tree.branches.containsKey("doot"), "Setting an item at a level 1 path on a new empty tree did not create a branch at the correct key.");
        assertTrue(tree.branches.get("doot").hasRootItem, "Setting an item at a level 1 path on a new empty tree did not set .hasRootItem to true on the created branch.");
        assertEquals(8, tree.branches.get("doot").rootItem, "Setting an item at a level 1 path on a new empty tree did not set .rootItem to that item on the created branch.");
    }
}