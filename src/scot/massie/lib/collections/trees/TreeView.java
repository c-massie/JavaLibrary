package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class TreeView<T extends Tree<TNode, TLeaf>, TNode, TLeaf> implements Tree<TNode, TLeaf>
{
    protected final T source;

    protected final TreePath<TNode> viewPath;

    protected final BiFunction<T, TreePath<TNode>, T> internalBranchGetter;

    public TreeView(T source, TreePath<TNode> viewPath, BiFunction<T, TreePath<TNode>, T> internalBranchGetter)
    {
        this.source = source;
        this.viewPath = viewPath;
        this.internalBranchGetter = internalBranchGetter;
    }

    // TO DO: Change these implementations to take into account the view path.

    @Override
    public boolean hasItems()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);
        return sourceBranch != null && sourceBranch.hasItems();
    }

    @Override
    public boolean hasNonRootItems()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);
        return sourceBranch != null && sourceBranch.hasNonRootItems();
    }

    @Override
    public boolean hasItemsAlong(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);
        return sourceBranch != null && sourceBranch.hasItemsAlong(path);
    }

    @Override
    public boolean hasNonRootItemsAlong(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);
        return sourceBranch != null && sourceBranch.hasNonRootItemsAlong(path);
    }

    @Override
    public boolean hasItemAt(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);
        return sourceBranch != null && sourceBranch.hasItemAt(path);
    }

    @Override
    public boolean hasRootItem()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);
        return sourceBranch != null && sourceBranch.hasRootItem();
    }

    @Override
    public TLeaf getRootItem() throws NoItemAtPathException
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null || sourceBranch.isEmptyAtRoot())
            throw new NoItemAtPathException(source, viewPath);

        return sourceBranch.getRootItem();
    }

    @Override
    public TLeaf getRootItemOr(TLeaf defaultItem)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getRootItemOr(defaultItem);
    }

    @Override
    public Object getRootItemOrDefaultOfAnyType(Object defaultItem)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getRootItemOrDefaultOfAnyType(defaultItem);
    }

    @Override
    public TLeaf getAt(TreePath<TNode> path) throws NoItemAtPathException
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

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
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getAtOr(path, defaultItem);
    }

    @Override
    public Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return defaultItem;

        return sourceBranch.getAtOrDefaultOfAnyType(path, defaultItem);
    }

    @Override
    public Collection<TLeaf> getItems()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItems();
    }

    @Override
    public List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsInOrder(comparator);
    }

    @Override
    public Collection<TLeaf> getItemsWhere(BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsWhere(test);
    }

    @Override
    public Collection<TLeaf> getItemsWherePath(Predicate<TreePath<TNode>> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsWherePath(test);
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator, BiPredicate<TreePath<TNode>, TLeaf> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsInOrderWhere(comparator, test);
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator, Predicate<TreePath<TNode>> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsInOrderWherePath(comparator, test);
    }

    @Override
    public Collection<TLeaf> getItemsUnderRoot()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsUnderRoot();
    }

    @Override
    public List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsUnderRootInOrder(comparator);
    }

    @Override
    public List<TLeaf> getItemsAlong(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsAlong(path);
    }

    @Override
    public List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getItemsUnderRootAlong(path);
    }

    @Override
    public Collection<TLeaf> getImmediateItems()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateItems();
    }

    @Override
    public List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateItemsInOrder(comparator);
    }

    @Override
    public Collection<TLeaf> getRootAndImmediateItems()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateItems();
    }

    @Override
    public List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateItemsInOrder(comparator);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntries()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntries();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesInOrder(comparator);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<TreeEntry<TNode, TLeaf>> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesWhere(test);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator, Predicate<TreeEntry<TNode, TLeaf>> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesInOrderWhere(comparator, test);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<TreePath<TNode>> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesWherePath(test);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator, Predicate<TreePath<TNode>> test)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesInOrderWherePath(comparator, test);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesUnderRoot();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesUnderRootInOrder(comparator);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesAlong(path);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getEntriesUnderRootAlong(path);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateEntries();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getImmediateEntriesInOrder(comparator);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getRootAndImmediateEntries();
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

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
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyMap();

        return sourceBranch.getBranches();
    }

    @Override
    public Collection<TreePath<TNode>> getPaths()
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyList();

        return sourceBranch.getPaths();
    }

    @Override
    public List<TreePath<TNode>> getPathsInOrder(Comparator<? extends TNode> comparator)
    {
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

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
        T sourceBranch = internalBranchGetter.apply(source, viewPath);

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

    // TO DO: Continue implementing from here.

    @Override
    public TLeaf setRootItem(TLeaf newItem)
    { return source.setRootItem(newItem); }

    @Override
    public TLeaf setRootItemIfAbsent(TLeaf newItem)
    { return source.setRootItemIfAbsent(newItem); }

    @Override
    public TLeaf setRootItemIf(TLeaf newItem, BiPredicate<TreePath<TNode>, TLeaf> test)
    { return source.setRootItemIf(newItem, test); }

    @Override
    public TLeaf setAt(TreePath<TNode> path, TLeaf newItem)
    { return source.setAt(path, newItem); }

    @Override
    public TLeaf setAtIfAbsent(TreePath<TNode> path, TLeaf newItem)
    { return source.setAtIfAbsent(path, newItem); }

    @Override
    public TLeaf setAtIf(TLeaf newItem, BiPredicate<TreePath<TNode>, TLeaf> test)
    { return source.setAtIf(newItem, test); }

    @Override
    public void clear()
    { source.clear(); }

    @Override
    public void clearUnderRoot()
    { source.clearUnderRoot(); }

    @Override
    public void clearWhere(BiPredicate<TreePath<TNode>, TLeaf> test)
    { source.clearWhere(test); }

    @Override
    public TLeaf clearRoot()
    { return source.clearRoot(); }

    @Override
    public TLeaf clearRootIf(BiPredicate<TreePath<TNode>, TLeaf> test)
    { return source.clearRootIf(test); }

    @Override
    public TLeaf clearAt(TreePath<TNode> path)
    { return source.clearAt(path); }

    @Override
    public TLeaf clearAtIf(TreePath<TNode> path, BiPredicate<TreePath<TNode>, TLeaf> test)
    { return source.clearAtIf(path, test); }

    @Override
    public List<TLeaf> toList()
    { return source.toList(); }

    @Override
    public List<TLeaf> toOrderedList(Comparator<? super TNode> comparator)
    { return source.toOrderedList(comparator); }

    @Override
    public Tree<TNode, TLeaf> withReversedKeys()
    { return source.withReversedKeys(); }

    @Override
    public int size()
    { return source.size(); }

    @Override
    public int countDepth()
    { return source.countDepth(); }

    @Override
    public String toTreeString()
    { return source.toTreeString(); }
}
