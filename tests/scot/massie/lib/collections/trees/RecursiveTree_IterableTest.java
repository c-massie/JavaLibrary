package scot.massie.lib.collections.trees;

import scot.massie.lib.javainterfacetests.IterableTest;
import scot.massie.lib.javainterfacetests.OrderedIterableTest;
import scot.massie.lib.utils.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class RecursiveTree_IterableTest
        extends IterableTest<TreeEntry<String, Integer>, RecursiveTree<String, Integer>>
{
    @Override
    public RecursiveTree<String, Integer> getEmptyIterable()
    {
        return new RecursiveTree<>();
    }

    @Override
    public RecursiveTree<String, Integer> getIterableWithSingleValue()
    {
        RecursiveTree<String, Integer> tree = new RecursiveTree<>();
        TreeEntry<String, Integer> singleTestValue = getSingleTestValue();
        tree.setAt(singleTestValue.getPath(), singleTestValue.getItem());
        return tree;
    }

    @Override
    public RecursiveTree<String, Integer> getIterableWithValues()
    {
        RecursiveTree<String, Integer> tree = new RecursiveTree<>();

        for(TreeEntry<String, Integer> testValue : getTestValues())
            tree.setAt(testValue.getPath(), testValue.getItem());
        
        return tree;
    }

    @Override
    public TreeEntry<String, Integer> getSingleTestValue()
    {
        return new TreeEntry<>(null, new TreePath<>("first", "second", "third"), 5);
    }

    @Override
    public List<TreeEntry<String, Integer>> getTestValues()
    {
        ArrayList<TreeEntry<String, Integer>> result = new ArrayList<>();

        result.add(new TreeEntry<>(null, TreePath.root(), 37));
        result.add(new TreeEntry<>(null, new TreePath<>("first"), 29));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "second"), 65));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "second", "third"), 71));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "second", "third", "fourth"), 11));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "second", "third", "fourth", "fifth"), 54));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "second", "third", "fourth", "fiveth"), 98));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "second", "third", "fourth", "notfive", "notsix"), 5));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "twoth"), 58));
        result.add(new TreeEntry<>(null, new TreePath<>("first", "twoth", "threeth"), 66));
        result.add(new TreeEntry<>(null, new TreePath<>("uno"), 44));
        result.add(new TreeEntry<>(null, new TreePath<>("uno", "dos"), 87));
        result.add(new TreeEntry<>(null, new TreePath<>("uno", "dos", "third"), 92));
        
        return result;
    }

    @Override
    public boolean areEqual(TreeEntry<String, Integer> a, TreeEntry<String, Integer> b)
    { return a.path.equals(b.path) && a.item.equals(b.item); }
}
