package scot.massie.lib.collections.trees;

import scot.massie.lib.utils.tuples.Pair;

class RecursiveTreeTest extends TreeTest
{

    @Override
    protected boolean allowsNull()
    { return true; }

    @Override
    protected Tree<String, Integer> getNewTree()
    { return new RecursiveTree<>(); }

    @SafeVarargs
    @Override
    protected final Tree<String, Integer> getNewTree(Pair<TreePath<String>, Integer>... items)
    {
        // Assumes that .setAt is working as expected.
        Tree<String, Integer> result = new RecursiveTree<>();

        for(Pair<TreePath<String>, Integer> item : items)
            result.setAt(item.getFirst(), item.getSecond());

        return result;
    }
}