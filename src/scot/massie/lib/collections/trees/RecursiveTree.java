package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class RecursiveTree<TNode, TLeaf> implements Tree<TNode, TLeaf>
{
    protected TLeaf rootItem = null;

    protected boolean hasRootItem = false;

    protected final Map<TNode, RecursiveTree<TNode, TLeaf>> branches = new HashMap<>();

    RecursiveTree<TNode, TLeaf> getInternalDirectBranch(TNode branchPathStart)
    { return branches.get(branchPathStart); }

    RecursiveTree<TNode, TLeaf> getInternalBranch(TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> current = this;

        for(TNode node : path.getNodes())
        {
            current = current.branches.get(node);

            if(current == null)
                return null;
        }

        return current;
    }

    RecursiveTree<TNode, TLeaf> getOrCreateInternalBranch(TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> current = this;

        for(TNode node : path.getNodes())
            current = current.branches.computeIfAbsent(node, tNode -> new RecursiveTree<>());

        return current;
    }

    void trim(TreePath<TNode> path)
    {
        List<RecursiveTree<TNode, TLeaf>> branchesAlongPath = new ArrayList<>(path.size());
        RecursiveTree<TNode, TLeaf> current = this;
        branchesAlongPath.add(current);

        for(TNode node : path.getNodes())
        {
            current = current.branches.get(node);

            if(current == null)
                break;

            branchesAlongPath.add(current);
        }

        for(int i = branchesAlongPath.size() - 1; i > 0; i--)
        {
            RecursiveTree<TNode, TLeaf> iBranch = branchesAlongPath.get(i);

            if(iBranch.hasItems())
                break;

            RecursiveTree<TNode, TLeaf> parentBranch = branchesAlongPath.get(i - 1);
            parentBranch.branches.remove(path.getNode(i - 1));
        }
    }

    @Override
    public boolean hasItems()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean hasNonRootItems()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean hasItemsAlong(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean hasNonRootItemsAlong(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean hasItemAt(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean hasRootItem()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf getRootItem() throws NoItemAtPathException
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf getRootItemOr(TLeaf defaultItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Object getRootItemOrDefaultOfAnyType(Object defaultItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf getAt(TreePath<TNode> path) throws NoItemAtPathException
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf getAtOr(TreePath<TNode> path, TLeaf defaultItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getItems()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getItemsWhere(BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getItemsWherePath(Predicate<TreePath<TNode>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator, BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator, Predicate<TreePath<TNode>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getItemsUnderRoot()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> getItemsAlong(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getImmediateItems()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TLeaf> getRootAndImmediateItems()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntries()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<TreeEntry<TNode, TLeaf>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator, Predicate<TreeEntry<TNode, TLeaf>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<TreePath<TNode>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator, Predicate<TreePath<TNode>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Tree<TNode, TLeaf> getBranch(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranches()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<TreePath<TNode>> getPaths()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TreePath<TNode>> getPathsInOrder(Comparator<? extends TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Tree<TNode, TLeaf> getBranchView(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranchViews()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf setRootItem(TLeaf newItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf setRootItemIfAbsent(TLeaf newItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf setRootItemIf(TLeaf newItem, BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf setAt(TreePath<TNode> path, TLeaf newItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf setAtIfAbsent(TreePath<TNode> path, TLeaf newItem)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf setAtIf(TreePath<TNode> path, TLeaf newItem, BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void clear()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void clearUnderRoot()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void clearWhere(BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void clearWherePath(Predicate<TreePath<TNode>> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf clearRoot()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf clearRootIf(BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf clearAt(TreePath<TNode> path)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public TLeaf clearAtIf(TreePath<TNode> path, BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> toList()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<TLeaf> toOrderedList(Comparator<? super TNode> comparator)
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Tree<TNode, TLeaf> withReversedKeys()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int size()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int countDepth()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public String toTreeString()
    {
        // TO DO: Write.
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
