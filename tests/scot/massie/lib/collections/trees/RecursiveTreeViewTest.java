package scot.massie.lib.collections.trees;

import scot.massie.lib.utils.tuples.Pair;

class RecursiveTreeViewTest extends TreeTest
{
    @Override
    protected boolean allowsNull()
    { return true; }

    @Override
    protected Tree<String, Integer> getNewTree()
    {
        RecursiveTree<String, Integer> tree = new RecursiveTree<>();
        return new RecursiveTreeView<>(tree, new TreePath<>("first", "second"));
    }

    @SafeVarargs
    @Override
    protected final Tree<String, Integer> getNewTree(Pair<TreePath<String>, Integer>... items)
    {
        // Assumes that .setAt is working as expected.
        RecursiveTree<String, Integer> tree = new RecursiveTree<>();
        Tree<String, Integer> view = new RecursiveTreeView<>(tree, new TreePath<>("first", "second"));

        for(Pair<TreePath<String>, Integer> item : items)
            view.setAt(item.getFirst(), item.getSecond());

        return view;
    }
}