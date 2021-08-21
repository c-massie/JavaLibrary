package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractRecursiveTreeView<T extends Tree<TNode, TLeaf>, TNode, TLeaf>
        implements Tree<TNode, TLeaf>
{
    protected final T source;

    protected final TreePath<TNode> viewPath;

    protected final TreePath<TNode> viewPathParent;

    public AbstractRecursiveTreeView(T source, TreePath<TNode> viewPath)
    {
        this.source = source;
        this.viewPath = viewPath;
        this.viewPathParent = viewPath.getParent();
    }

    public abstract T getInternalBranch(T sourceTree, TreePath<TNode> branchPath);

    public abstract T getOrCreateInternalBranch(T sourceTree, TreePath<TNode> branchPath);

    public abstract void trim(T sourceTree, TreePath<TNode> branchPath);

    public abstract T getNewEmptyTree();

    @Override
    public boolean hasItems()
    {
        T sourceBranch = getInternalBranch(source, viewPath);
        return sourceBranch != null && sourceBranch.hasItems();
    }

    @Override
    public boolean hasNonRootItems()
    {
        T sourceBranch = getInternalBranch(source, viewPath);
        return sourceBranch != null && sourceBranch.hasNonRootItems();
    }

    @Override
    public boolean hasItemsAlong(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);
        return sourceBranch != null && sourceBranch.hasItemsAlong(path);
    }

    @Override
    public boolean hasNonRootItemsAlong(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);
        return sourceBranch != null && sourceBranch.hasNonRootItemsAlong(path);
    }

    @Override
    public boolean hasItemAt(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);
        return sourceBranch != null && sourceBranch.hasItemAt(path);
    }

    @Override
    public boolean hasRootItem()
    {
        T sourceBranch = getInternalBranch(source, viewPath);
        return sourceBranch != null && sourceBranch.hasRootItem();
    }

    @Override
    public TLeaf getRootItem() throws NoItemAtPathException
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null || sourceBranch.isEmptyAtRoot())
            throw new NoItemAtPathException(source, viewPath);

        return sourceBranch.getRootItem();
    }

    @Override
    public TLeaf getRootItemOr(TLeaf defaultItem)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getRootItemOr(defaultItem);
    }

    @Override
    public Object getRootItemOrDefaultOfAnyType(Object defaultItem)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getRootItemOrDefaultOfAnyType(defaultItem);
    }

    @Override
    public TLeaf getAt(@SuppressWarnings("BoundedWildcard") TreePath<TNode> path) throws NoItemAtPathException
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            throw new NoItemAtPathException(source, viewPath.appendedWith(path));

        try
        { return sourceBranch.getAt(path); }
        catch(NoItemAtPathException e)
        { throw new NoItemAtPathException(source, viewPath.appendedWith(path)); }
    }

    @Override
    public TLeaf getAtOr(TreePath<TNode> path, TLeaf defaultItem)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getAtOr(path, defaultItem);
    }

    @Override
    public Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getAtOrDefaultOfAnyType(path, defaultItem);
    }

    @Override
    public Collection<TLeaf> getItems()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItems();
    }

    @Override
    public List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsInOrder(comparator);
    }

    @Override
    public Collection<TLeaf> getItemsWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsWhere(test);
    }

    @Override
    public Collection<TLeaf> getItemsWherePath(Predicate<? super TreePath<TNode>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsWherePath(test);
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator,
                                                  BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsInOrderWhere(comparator, test);
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator,
                                                      Predicate<? super TreePath<TNode>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsInOrderWherePath(comparator, test);
    }

    @Override
    public Collection<TLeaf> getItemsUnderRoot()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsUnderRoot();
    }

    @Override
    public List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsUnderRootInOrder(comparator);
    }

    @Override
    public List<TLeaf> getItemsAlong(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsAlong(path);
    }

    @Override
    public List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsUnderRootAlong(path);
    }

    @Override
    public Collection<TLeaf> getImmediateItems()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateItems();
    }

    @Override
    public List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateItemsInOrder(comparator);
    }

    @Override
    public Collection<TLeaf> getRootAndImmediateItems()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateItems();
    }

    @Override
    public List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateItemsInOrder(comparator);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntries()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntries();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesInOrder(comparator);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesWhere(test);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator,
                                                                Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesInOrderWhere(comparator, test);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<? super TreePath<TNode>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesWherePath(test);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator,
                                                                    Predicate<? super TreePath<TNode>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesInOrderWherePath(comparator, test);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesUnderRoot();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesUnderRootInOrder(comparator);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesAlong(path);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesUnderRootAlong(path);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateEntries();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateEntriesInOrder(comparator);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateEntries();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateEntriesInOrder(comparator);
    }

    @Override
    public Tree<TNode, TLeaf> getBranch(TreePath<TNode> path)
    { return source.getBranch(viewPath.appendedWith(path)); }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranches()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyMap();

        return sourceBranch.getBranches();
    }

    @Override
    public Collection<TreePath<TNode>> getPaths()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getPaths();
    }

    @Override
    public List<TreePath<TNode>> getPathsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getPathsInOrder(comparator);
    }

    @Override
    public Tree<TNode, TLeaf> getBranchView(TreePath<TNode> path)
    { return source.getBranchView(viewPath.appendedWith(path)); }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranchViews()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyMap();

        Map<TNode, Tree<TNode, TLeaf>> branchViews = sourceBranch.getBranchViews();
        Map<TNode, Tree<TNode, TLeaf>> result = new HashMap<>();

        for(Map.Entry<TNode, Tree<TNode, TLeaf>> entry : branchViews.entrySet())
        {
            TreePath<TNode> appendedTreePath = viewPath.appendedWith(entry.getKey());
            result.put(entry.getKey(), source.getBranchView(appendedTreePath));
        }

        return result;
    }

    @Override
    public TLeaf setRootItem(TLeaf newItem)
    { return getOrCreateInternalBranch(source, viewPath).setRootItem(newItem); }

    @Override
    public TLeaf setRootItemIfAbsent(TLeaf newItem)
    { return getOrCreateInternalBranch(source, viewPath).setRootItemIfAbsent(newItem); }

    @Override
    public TLeaf setRootItemIf(TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    { return getOrCreateInternalBranch(source, viewPath).setRootItemIf(newItem, test); }

    @Override
    public TLeaf setAt(TreePath<TNode> path, TLeaf newItem)
    { return getOrCreateInternalBranch(source, viewPath).setAt(path, newItem); }

    @Override
    public TLeaf setAtIfAbsent(TreePath<TNode> path, TLeaf newItem)
    { return getOrCreateInternalBranch(source, viewPath).setAtIfAbsent(path, newItem); }

    @Override
    public TLeaf setAtIf(TreePath<TNode> path, TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
        {
            if(test.test(path, null))
                getOrCreateInternalBranch(source, viewPath).setAt(path, newItem);

            return null;
        }

        return sourceBranch.setAtIf(path, newItem, test);
    }

    @Override
    public void clear()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return;

        sourceBranch.clear();
        trim(source, viewPath);
    }

    @Override
    public void clearUnderRoot()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return;

        sourceBranch.clearUnderRoot();
        trim(source, viewPath);
    }

    @Override
    public void clearWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return;

        sourceBranch.clearWhere(test);
        trim(source, viewPath);
    }

    @Override
    public void clearWherePath(Predicate<? super TreePath<TNode>> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return;

        sourceBranch.clearWherePath(test);
        trim(source, viewPath);
    }

    @Override
    public TLeaf clearRoot()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return null;

        TLeaf result = sourceBranch.clearRoot();
        trim(source, viewPath);
        return result;
    }

    @Override
    public TLeaf clearRootIf(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return null;

        TLeaf result = sourceBranch.clearRootIf(test);
        trim(source, viewPath);
        return result;
    }

    @Override
    public TLeaf clearAtIf(@SuppressWarnings("BoundedWildcard") TreePath<TNode> path,
                           BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return null;

        TLeaf result = sourceBranch.clearAtIf(path, test);
        trim(source, viewPath.appendedWith(path));
        return result;
    }

    @Override
    public List<TLeaf> toList()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.toList();
    }

    @Override
    public List<TLeaf> toOrderedList(Comparator<? super TNode> comparator)
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.toOrderedList(comparator);
    }

    @Override
    public Tree<TNode, TLeaf> withReversedKeys()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return getNewEmptyTree();

        return sourceBranch.withReversedKeys();
    }

    @Override
    public int size()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return 0;

        return sourceBranch.size();
    }

    @Override
    public int countDepth()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return 0;

        return sourceBranch.countDepth();
    }

    @Override
    public Iterator<TreeEntry<TNode, TLeaf>> iterator()
    { return source.iterator(); }

    @Override
    public void forEach(Consumer<? super TreeEntry<TNode, TLeaf>> action)
    { source.forEach(action); }
}
