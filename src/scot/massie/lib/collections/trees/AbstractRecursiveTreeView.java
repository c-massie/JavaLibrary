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

import static scot.massie.lib.utils.ControlFlowUtils.*;

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
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::hasItems, () -> false); }

    @Override
    public boolean hasNonRootItems()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::hasNonRootItems, () -> false); }

    @Override
    public boolean hasItemsAlong(TreePath<TNode> path)
    { return getFromUnlessNull(getInternalBranch(source, viewPath), b -> b.hasItemsAlong(path), () -> false); }

    @Override
    public boolean hasNonRootItemsAlong(TreePath<TNode> path)
    { return getFromUnlessNull(getInternalBranch(source, viewPath), b -> b.hasNonRootItemsAlong(path), () -> false); }

    @Override
    public boolean hasItemAt(TreePath<TNode> path)
    { return getFromUnlessNull(getInternalBranch(source, viewPath), b -> b.hasItemAt(path), () -> false); }

    @Override
    public boolean hasRootItem()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::hasRootItem, () -> false); }

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
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getRootItemOr(defaultItem),
                                 () -> defaultItem);
    }

    @Override
    public Object getRootItemOrDefaultOfAnyType(Object defaultItem)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getRootItemOrDefaultOfAnyType(defaultItem),
                                 () -> defaultItem);
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
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getAtOr(path, defaultItem),
                                 () -> defaultItem);
    }

    @Override
    public Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getAtOrDefaultOfAnyType(path, defaultItem),
                                 () -> defaultItem);
    }

    @Override
    public Collection<TLeaf> getItems()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::getItems, Collections::emptyList); }

    @Override
    public List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getItemsWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsWhere(test),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getItemsWherePath(Predicate<? super TreePath<TNode>> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsWherePath(test),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator,
                                                  BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsInOrderWhere(comparator, test),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator,
                                                      Predicate<? super TreePath<TNode>> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsInOrderWherePath(comparator, test),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getItemsUnderRoot()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::getItemsUnderRoot, Collections::emptyList); }

    @Override
    public List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsUnderRootInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public List<TLeaf> getItemsAlong(TreePath<TNode> path)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsAlong(path),
                                 Collections::emptyList);
    }

    @Override
    public List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getItemsUnderRootAlong(path),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getImmediateItems()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::getImmediateItems, Collections::emptyList); }

    @Override
    public List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getImmediateItemsInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TLeaf> getRootAndImmediateItems()
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 Tree::getRootAndImmediateItems,
                                 Collections::emptyList);
    }

    @Override
    public List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getRootAndImmediateItemsInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntries()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::getEntries, Collections::emptyList); }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesWhere(test),
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator,
                                                                Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesInOrderWhere(comparator, test),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<? super TreePath<TNode>> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesWherePath(test),
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator,
                                                                    Predicate<? super TreePath<TNode>> test)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesInOrderWherePath(comparator, test),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot()
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 Tree::getEntriesUnderRoot,
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesUnderRootInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesAlong(path),
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getEntriesUnderRootAlong(path),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries()
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 Tree::getImmediateEntries,
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getImmediateEntriesInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries()
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 Tree::getRootAndImmediateEntries,
                                 Collections::emptyList);
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getRootAndImmediateEntriesInOrder(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Tree<TNode, TLeaf> getBranch(TreePath<TNode> path)
    { return source.getBranch(viewPath.appendedWith(path)); }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranches()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::getBranches, Collections::emptyMap); }

    @Override
    public Collection<TreePath<TNode>> getPaths()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::getPaths, Collections::emptyList); }

    @Override
    public List<TreePath<TNode>> getPathsInOrder(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                 b -> b.getPathsInOrder(comparator),
                                 Collections::emptyList);
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
    { ifNotNull(getInternalBranch(source, viewPath), b -> { b.clear(); trim(source, viewPath); }); }

    @Override
    public void clearUnderRoot()
    { ifNotNull(getInternalBranch(source, viewPath), b -> { b.clearUnderRoot(); trim(source, viewPath); }); }

    @Override
    public void clearWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    { ifNotNull(getInternalBranch(source, viewPath), b -> { b.clearWhere(test); trim(source, viewPath); }); }

    @Override
    public void clearWherePath(Predicate<? super TreePath<TNode>> test)
    { ifNotNull(getInternalBranch(source, viewPath), b -> { b.clearWherePath(test); trim(source, viewPath); }); }

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
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::toList, Collections::emptyList); }

    @Override
    public List<TLeaf> toOrderedList(Comparator<? super TNode> comparator)
    {
        return getFromUnlessNull(getInternalBranch(source, viewPath),
                                    b -> b.toOrderedList(comparator),
                                 Collections::emptyList);
    }

    @Override
    public Map<TreePath<TNode>, TLeaf> toMap()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::toMap, Collections::emptyMap); }

    @Override
    public Tree<TNode, TLeaf> withReversedKeys()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::withReversedKeys, this::getNewEmptyTree); }

    @Override
    public int size()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::size, () -> 0); }

    @Override
    public int countDepth()
    { return getFromUnlessNull(getInternalBranch(source, viewPath), Tree::countDepth, () -> 0); }

    @Override
    public Iterator<TreeEntry<TNode, TLeaf>> iterator()
    {
        T sourceBranch = getInternalBranch(source, viewPath);

        if(sourceBranch == null)
            return Collections.emptyIterator();

        return new Iterator<TreeEntry<TNode, TLeaf>>()
        {
            private final Iterator<TreeEntry<TNode, TLeaf>> sourceBranchIterator = sourceBranch.iterator();

            @Override
            public boolean hasNext()
            { return sourceBranchIterator.hasNext(); }

            @Override
            public TreeEntry<TNode, TLeaf> next()
            {
                TreeEntry<TNode, TLeaf> sourceNext = sourceBranchIterator.next();
                return new TreeEntry<>(AbstractRecursiveTreeView.this, sourceNext.getPath(), sourceNext.getItem());
            }
        };
    }

    @Override
    public void forEach(Consumer<? super TreeEntry<TNode, TLeaf>> action)
    {
        ifNotNull(getInternalBranch(source, viewPath),
                  b -> b.forEach(entry -> action.accept(new TreeEntry<>(this, entry.getPath(), entry.getItem()))));
    }
}
