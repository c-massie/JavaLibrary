package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RecursiveTree<TNode, TLeaf> implements Tree<TNode, TLeaf>
{
    //region fields
    protected TLeaf rootItem = null;

    protected boolean hasRootItem = false;

    protected final Map<TNode, RecursiveTree<TNode, TLeaf>> branches = new HashMap<>();
    //endregion

    //region initialisation
    public RecursiveTree()
    {}

    public RecursiveTree(Tree<TNode, TLeaf> other)
    {
        for(TreeEntry<TNode, TLeaf> entry : other.getEntries())
            setAt(entry.getPath(), entry.getItem());
    }
    //endregion

    //region methods
    //region get internal branches
    RecursiveTree<TNode, TLeaf> getInternalDirectBranch(TNode branchPathStart)
    { return branches.get(branchPathStart); }

    RecursiveTree<TNode, TLeaf> getInternalBranch(@SuppressWarnings("BoundedWildcard")
                                                          /* Should not be TreePath<? extends TNode> */
                                                          TreePath<TNode> path)
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

    RecursiveTree<TNode, TLeaf> getOrCreateInternalBranch(@SuppressWarnings("BoundedWildcard")
                                                                  /* Should not be TreePath<? extends TNode> */
                                                                  TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> current = this;

        for(TNode node : path.getNodes())
            current = current.branches.computeIfAbsent(node, tNode -> new RecursiveTree<>());

        return current;
    }
    //endregion

    //region trim
    void trim(@SuppressWarnings("BoundedWildcard")
                      /* Should not be TreePath<? extends TNode> */
                      TreePath<TNode> path)
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

    @SuppressWarnings("PackageVisibleField") // Is a POD class.
    static class BranchWithAlreadyCoveredState<TNode, TLeaf>
    {
        final RecursiveTree<TNode, TLeaf> branch;
        boolean alreadyCovered = false;

        public BranchWithAlreadyCoveredState(RecursiveTree<TNode, TLeaf> branch)
        { this.branch = branch; }
    }

    void trim()
    {
        Deque<BranchWithAlreadyCoveredState<TNode, TLeaf>> stack = new ArrayDeque<>();
        stack.add(new BranchWithAlreadyCoveredState<>(this));

        while(!stack.isEmpty())
        {
            /*
                initialStackSize = stack size

                for each sub-branch of branch:
                    if the sub-branch has no root item and no branches:
                        remove it from the branch
                    else if (branch has not already been covered) and (sub-branch has branches):
                        add sub-branch to the stack

                if stack size == initialStackSize:
                    remove branch from stack
             */

            BranchWithAlreadyCoveredState<TNode, TLeaf> branchWithAlreadyCoveredState = stack.getLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithAlreadyCoveredState.branch;
            boolean alreadyCovered = branchWithAlreadyCoveredState.alreadyCovered;

            int initialStackSize = stack.size();

            for(Iterator<RecursiveTree<TNode, TLeaf>> iter = branch.branches.values().iterator(); iter.hasNext(); )
            {
                RecursiveTree<TNode, TLeaf> subBranch = iter.next();

                if(subBranch.branches.isEmpty())
                {
                    if(!subBranch.hasRootItem)
                        iter.remove();
                }
                else if(!alreadyCovered)
                    stack.add(new BranchWithAlreadyCoveredState<>(subBranch));
            }

            if(stack.size() == initialStackSize) // If no sub-branches have been added to the stack
                stack.remove();
        }
    }
    //endregion

    //region loop over items
    void forEachItem(Consumer<? super TLeaf> f, boolean includeRoot)
    {
        Deque<RecursiveTree<TNode, TLeaf>> branchStack
                = new ArrayDeque<>(includeRoot ? Collections.singleton(this) : this.branches.values());

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> current = branchStack.removeLast();
            branchStack.addAll(current.branches.values());

            if(current.hasRootItem)
                f.accept(current.rootItem);
        }
    }

    void forEachItemInOrder(Comparator<? super TNode> comparator,
                            Consumer<? super TLeaf> f,
                            boolean includeRoot)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<RecursiveTree<TNode, TLeaf>> branchStack = new ArrayDeque<>();

        if(includeRoot)
            branchStack.add(this);
        else
            branches.entrySet()
                    .stream()
                    .sorted(mapEntryComparator)
                    .forEachOrdered(x -> branchStack.add(x.getValue()));

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> branch = branchStack.removeLast();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(x -> branchStack.add(x.getValue()));

            if(branch.hasRootItem)
                f.accept(branch.rootItem);
        }
    }

    void forEachItemWithPath(BiConsumer<? super TreePath<TNode>, ? super TLeaf> f,  boolean includeRoot)
    {
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();

        if(includeRoot)
            branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        else
            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), new TreePath<>(entry.getKey())));

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem)
                f.accept(path, branch.rootItem);
        }
    }

    void forEachItemWithPathInOrder(Comparator<? super TNode> comparator,
                                    BiConsumer<? super TreePath<TNode>, ? super TLeaf> f,
                                    boolean includeRoot)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();

        if(includeRoot)
            branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        else
        {
            //noinspection CodeBlock2Expr Clearer as a block.
            branches.entrySet()
                    .stream()
                    .sorted(mapEntryComparator)
                    .forEachOrdered(x ->
            { branchStack.add(new TreeBranchWithPath<>(x.getValue(), new TreePath<>(x.getKey()))); });
        }

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            //noinspection CodeBlock2Expr Clearer as a block.
            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(x ->
            { branchStack.add(new TreeBranchWithPath<>(x.getValue(), path.appendedWith(x.getKey()))); });

            if(branch.hasRootItem)
                f.accept(path, branch.rootItem);
        }
    }

    void forEachItem(Consumer<? super TLeaf> f)
    { forEachItem(f, true); }

    void forEachItemInOrder(Comparator<? super TNode> comparator, Consumer<? super TLeaf> f)
    { forEachItemInOrder(comparator, f, true); }

    void forEachItemUnderRoot(Consumer<? super TLeaf> f)
    { forEachItem(f, false); }

    void forEachItemUnderRootInOrder(Comparator<? super TNode> comparator, Consumer<? super TLeaf> f)
    { forEachItemInOrder(comparator, f, false); }

    void forEachItemWithPath(BiConsumer<? super TreePath<TNode>, ? super TLeaf> f)
    { forEachItemWithPath(f, true); }

    void forEachItemWithPathInOrder(Comparator<? super TNode> comparator,
                                    BiConsumer<? super TreePath<TNode>, ? super TLeaf> f)
    { forEachItemWithPathInOrder(comparator, f, true); }

    void forEachItemWithPathUnderRoot(BiConsumer<? super TreePath<TNode>, ? super TLeaf> f)
    { forEachItemWithPath(f, false); }

    void forEachItemWithPathUnderRootInOrder(Comparator<? super TNode> comparator,
                                             BiConsumer<? super TreePath<TNode>, ? super TLeaf> f)
    { forEachItemWithPathInOrder(comparator, f, false); }
    //endregion

    //region Tree implementation
    @Override
    public boolean hasItems()
    {
        return hasRootItem || !branches.isEmpty();
        // Assumes that tree has been trimmed, and that there are no branches with no root items and no sub-branches.
    }

    @Override
    public boolean hasNonRootItems()
    { return !branches.isEmpty(); } // Assumes that tree is trimmed

    @Override
    public boolean hasItemsAlong(@SuppressWarnings("BoundedWildcard") TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> branch = this;

        for(TNode node : path.getNodes())
        {
            if(branch.hasRootItem)
                return true;

            branch = branch.branches.get(node);

            if(branch == null)
                return false;
        }

        //noinspection RedundantIfStatement Is a step in a process, the final result after all steps is false.
        if(branch.hasRootItem)
            return true;

        return false;
    }

    @Override
    public boolean hasNonRootItemsAlong(@SuppressWarnings("BoundedWildcard") TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> branch = this;

        for(TNode node : path.getNodes())
        {
            branch = branch.branches.get(node);

            if(branch == null)
                return false;

            if(branch.hasRootItem)
                return true;
        }

        return false;
    }

    @Override
    public boolean hasItemAt(TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);
        return (branch != null) && (branch.hasRootItem);
    }

    @Override
    public boolean hasRootItem()
    { return hasRootItem; }

    @Override
    public TLeaf getRootItem() throws NoItemAtPathException
    {
        if(!hasRootItem)
            throw new NoItemAtPathException(this, TreePath.root());

        return rootItem;
    }

    @Override
    public TLeaf getRootItemOr(TLeaf defaultItem)
    { return hasRootItem ? rootItem : defaultItem; }

    @Override
    public Object getRootItemOrDefaultOfAnyType(Object defaultItem)
    { return hasRootItem ? rootItem : defaultItem; }

    @Override
    public TLeaf getAt(TreePath<TNode> path) throws NoItemAtPathException
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);

        if(branch == null || !branch.hasRootItem)
            throw new NoItemAtPathException(this, path);

        return branch.rootItem;
    }

    @Override
    public TLeaf getAtOr(TreePath<TNode> path, TLeaf defaultItem)
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);
        return ((branch == null) || (!branch.hasRootItem)) ? defaultItem : branch.rootItem;
    }

    @Override
    public Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem)
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);
        return ((branch == null) || (!branch.hasRootItem)) ? defaultItem : branch.rootItem;
    }

    @Override
    public Collection<TLeaf> getItems()
    { return toList(); }

    @Override
    public List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator)
    { return toOrderedList(comparator); }

    @Override
    public Collection<TLeaf> getItemsWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        Collection<TLeaf> result = new ArrayList<>();
        forEachItemWithPath((path, item) -> { if(test.test(path, item)) result.add(item); });
        return result;
    }

    @Override
    public Collection<TLeaf> getItemsWherePath(Predicate<? super TreePath<TNode>> test)
    {
        Collection<TLeaf> result = new ArrayList<>();
        forEachItemWithPath((path, item) -> { if(test.test(path)) result.add(item); });
        return result;
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator,
                                                  BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        Collection<TLeaf> result = new ArrayList<>();
        forEachItemWithPathInOrder(comparator, (path, item) -> { if(test.test(path, item)) result.add(item); });
        return result;
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator,
                                                      Predicate<? super TreePath<TNode>> test)
    {
        Collection<TLeaf> result = new ArrayList<>();
        forEachItemWithPathInOrder(comparator, (path, item) -> { if(test.test(path)) result.add(item); });
        return result;
    }

    @Override
    public Collection<TLeaf> getItemsUnderRoot()
    {
        Collection<TLeaf> result = new ArrayList<>();
        forEachItemUnderRoot(result::add);
        return result;
    }

    @Override
    public List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        List<TLeaf> result = new ArrayList<>();
        forEachItemUnderRootInOrder(comparator, result::add);
        return result;
    }

    @Override
    public List<TLeaf> getItemsAlong(@SuppressWarnings("BoundedWildcard") TreePath<TNode> path)
    {
        List<TLeaf> result = new ArrayList<>();
        RecursiveTree<TNode, TLeaf> current = this;

        if(hasRootItem)
            result.add(rootItem);

        for(TNode node : path.getNodes())
        {
            current = current.branches.get(node);

            if(current == null)
                return result;

            if(current.hasRootItem)
                result.add(current.rootItem);
        }

        return result;
    }

    @Override
    public List<TLeaf> getItemsUnderRootAlong(@SuppressWarnings("BoundedWildcard") TreePath<TNode> path)
    {
        List<TLeaf> result = new ArrayList<>();
        RecursiveTree<TNode, TLeaf> current = this;

        for(TNode node : path.getNodes())
        {
            current = current.branches.get(node);

            if(current == null)
                return result;

            if(current.hasRootItem)
                result.add(current.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getImmediateItems()
    {
        Collection<TLeaf> result = new ArrayList<>();

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            if(branch.hasRootItem)
                result.add(branch.rootItem);

        return result;
    }

    @Override
    public List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        List<TLeaf> result = new ArrayList<>();

        branches.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(comparator))
                .forEachOrdered(x ->
        {
            if(x.getValue().hasRootItem)
                result.add(x.getValue().rootItem);
        });

        return result;
    }

    @Override
    public Collection<TLeaf> getRootAndImmediateItems()
    {
        Collection<TLeaf> result = new ArrayList<>();

        if(hasRootItem)
            result.add(rootItem);

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            if(branch.hasRootItem)
                result.add(branch.rootItem);

        return result;
    }

    @Override
    public List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator)
    {
        List<TLeaf> result = new ArrayList<>();

        if(hasRootItem)
            result.add(rootItem);

        branches.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(comparator))
                .forEachOrdered(x ->
        {
            if(x.getValue().hasRootItem)
                result.add(x.getValue().rootItem);
        });

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntries()
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        forEachItemWithPath((path, item) -> result.add(new TreeEntry<>(this, path, item)));
        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        forEachItemWithPathInOrder(comparator, (path, item) -> result.add(new TreeEntry<>(this, path, item)));
        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        forEachItemWithPath((path, item) ->
        {
            TreeEntry<TNode, TLeaf> entry = new TreeEntry<>(this, path, item);

            if(test.test(entry))
                result.add(entry);
        });

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        forEachItemWithPath((path, item) ->
        {
            if(test.test(path, item))
                result.add(new TreeEntry<>(this, path, item));
        });

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator,
                                                                Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        forEachItemWithPathInOrder(comparator, (path, item) ->
        {
            TreeEntry<TNode, TLeaf> entry = new TreeEntry<>(this, path, item);

            if(test.test(entry))
                result.add(entry);
        });

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(
            Comparator<? super TNode> comparator,
            BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        forEachItemWithPathInOrder(comparator, (path, item) ->
        {
            if(test.test(path, item))
                result.add(new TreeEntry<>(this, path, item));
        });

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<? super TreePath<TNode>> test)
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        forEachItemWithPath((path, item) -> { if(test.test(path)) result.add(new TreeEntry<>(this, path, item)); });
        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator,
                                                                    Predicate<? super TreePath<TNode>> test)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        forEachItemWithPathInOrder(comparator, (path, item) ->
        {
            if(test.test(path))
                result.add(new TreeEntry<>(this, path, item));
        });

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot()
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        forEachItemWithPathUnderRoot((path, item) -> result.add(new TreeEntry<>(this, path, item)));
        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        forEachItemWithPathUnderRootInOrder(comparator, (path, item) -> result.add(new TreeEntry<>(this, path, item)));
        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        RecursiveTree<TNode, TLeaf> current = this;

        if(hasRootItem)
            result.add(new TreeEntry<>(this, TreePath.root(), rootItem));

        for(int i = 0; i < path.size(); i++)
        {
            TNode node = path.getNode(i);
            current = current.branches.get(node);

            if(current == null)
                return result;

            if(current.hasRootItem)
                result.add(new TreeEntry<>(this, path.truncateTo(i + 1), current.rootItem));
        }

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();
        RecursiveTree<TNode, TLeaf> current = this;

        for(int i = 0; i < path.size(); i++)
        {
            TNode node = path.getNode(i);
            current = current.branches.get(node);

            if(current == null)
                return result;

            if(current.hasRootItem)
                result.add(new TreeEntry<>(this, path.truncateTo(i + 1), current.rootItem));
        }

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries()
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
            if(entry.getValue().hasRootItem)
                result.add(new TreeEntry<>(this, new TreePath<>(entry.getKey()), entry.getValue().rootItem));

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        branches.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(comparator))
                .forEachOrdered(entry ->
        {
            if(entry.getValue().hasRootItem)
                result.add(new TreeEntry<>(RecursiveTree.this,
                                           new TreePath<>(entry.getKey()),
                                           entry.getValue().rootItem));
        });

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries()
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        if(hasRootItem)
            result.add(new TreeEntry<>(this, TreePath.root(), rootItem));

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
            if(entry.getValue().hasRootItem)
                result.add(new TreeEntry<>(this, new TreePath<>(entry.getKey()), entry.getValue().rootItem));

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        if(hasRootItem)
            result.add(new TreeEntry<>(this, TreePath.root(), rootItem));

        branches.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(comparator))
                .forEachOrdered(entry ->
        {
            if(entry.getValue().hasRootItem)
                result.add(new TreeEntry<>(RecursiveTree.this,
                                           new TreePath<>(entry.getKey()),
                                           entry.getValue().rootItem));
        });

        return result;
    }

    @Override
    public Tree<TNode, TLeaf> getBranch(TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> internalBranch = getInternalBranch(path);
        return internalBranch == null ? new RecursiveTree<>() : new RecursiveTree<>(internalBranch);
    }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranches()
    {
        Map<TNode, Tree<TNode, TLeaf>> result = new HashMap<>();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
            result.put(entry.getKey(), new RecursiveTree<>(entry.getValue()));

        return result;
    }

    @Override
    public Collection<TreePath<TNode>> getPaths()
    {
        Collection<TreePath<TNode>> result = new ArrayList<>();
        forEachItemWithPath((path, item) -> result.add(path));
        return result;
    }

    @Override
    public List<TreePath<TNode>> getPathsInOrder(Comparator<? super TNode> comparator)
    {
        List<TreePath<TNode>> result = new ArrayList<>();
        forEachItemWithPathInOrder(comparator, (path, item) -> result.add(path));
        return result;
    }

    @Override
    public Tree<TNode, TLeaf> getBranchView(TreePath<TNode> path)
    { return new RecursiveTreeView<>(this, path); }

    @Override
    public Map<TNode, Tree<TNode, TLeaf>> getBranchViews()
    {
        Map<TNode, Tree<TNode, TLeaf>> result = new HashMap<>();

        for(TNode node : branches.keySet())
            result.put(node, new RecursiveTreeView<>(this, new TreePath<>(node)));

        return result;
    }

    @Override
    public TLeaf setRootItem(TLeaf newItem)
    {
        TLeaf old = hasRootItem ? rootItem : null;
        hasRootItem = true;
        rootItem = newItem;
        return old;
    }

    @Override
    public TLeaf setRootItemIfAbsent(TLeaf newItem)
    {
        TLeaf old = hasRootItem ? rootItem : null;

        if(!hasRootItem)
        {
            hasRootItem = true;
            rootItem = newItem;
        }

        return old;
    }

    @Override
    public TLeaf setRootItemIf(TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        TLeaf old = hasRootItem ? rootItem : null;

        if(test.test(TreePath.root(), old))
        {
            hasRootItem = true;
            rootItem = newItem;
        }

        return old;
    }

    @Override
    public TLeaf setAt(TreePath<TNode> path, TLeaf newItem)
    {
        RecursiveTree<TNode, TLeaf> branch = getOrCreateInternalBranch(path);
        TLeaf old = branch.hasRootItem ? branch.rootItem : null;
        branch.hasRootItem = true;
        branch.rootItem = newItem;
        return old;
    }

    @Override
    public TLeaf setAtIfAbsent(TreePath<TNode> path, TLeaf newItem)
    {
        RecursiveTree<TNode, TLeaf> branch = getOrCreateInternalBranch(path);
        TLeaf old = branch.hasRootItem ? branch.rootItem : null;

        if(!branch.hasRootItem)
        {
            branch.hasRootItem = true;
            branch.rootItem = newItem;
        }

        return old;
    }

    @Override
    public TLeaf setAtIf(TreePath<TNode> path, TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);
        TLeaf old = (branch != null && branch.hasRootItem) ? (branch.rootItem) : (null);

        if(test.test(path, old))
        {
            if(branch == null)
                branch = getOrCreateInternalBranch(path);

            branch.hasRootItem = true;
            branch.rootItem = newItem;
        }

        return old;
    }

    @Override
    public void clear()
    {
        hasRootItem = false;
        rootItem = null;
        branches.clear();
    }

    @Override
    public void clearUnderRoot()
    { branches.clear(); }

    @Override
    public void clearWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        Collection<TreeEntry<TNode, TLeaf>> entries = getEntriesWhere(test);

        for(TreeEntry<TNode, TLeaf> entry : entries)
            getInternalBranch(entry.getPath()).clearRoot();

        trim();
    }

    @Override
    public void clearWherePath(Predicate<? super TreePath<TNode>> test)
    {
        Collection<TreePath<TNode>> paths = getPaths();

        for(TreePath<TNode> path : paths)
            if(test.test(path))
                getInternalBranch(path).clearRoot();

        trim();
    }

    @Override
    public TLeaf clearRoot()
    {
        TLeaf old = hasRootItem ? rootItem : null;
        hasRootItem = false;
        rootItem = null;
        return old;
    }

    @Override
    public TLeaf clearRootIf(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        TLeaf old = hasRootItem ? rootItem : null;

        if(test.test(TreePath.root(), old))
        {
            hasRootItem = false;
            rootItem = null;
        }

        return old;
    }

    @Override
    public TLeaf clearAt(TreePath<TNode> path)
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);

        if(branch == null || !branch.hasRootItem)
            return null;

        TLeaf old = branch.clearRoot();

        if(branch.isEmptyUnderRoot())
            trim(path);

        return old;
    }

    @Override
    public TLeaf clearAtIf(TreePath<TNode> path, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        RecursiveTree<TNode, TLeaf> branch = getInternalBranch(path);

        if(branch == null || !branch.hasRootItem)
            return null;

        TLeaf old = branch.rootItem;

        if(!test.test(path, old))
            return old;

        branch.clearRoot();

        if(branch.isEmptyUnderRoot())
            trim(path);

        return old;
    }

    @Override
    public List<TLeaf> toList()
    {
        List<TLeaf> result = new ArrayList<>();
        forEachItem(result::add);
        return result;
    }

    @Override
    public List<TLeaf> toOrderedList(Comparator<? super TNode> comparator)
    {
        List<TLeaf> result = new ArrayList<>();
        forEachItemInOrder(comparator, result::add);
        return result;
    }

    @Override
    public Tree<TNode, TLeaf> withReversedKeys()
    {
        Tree<TNode, TLeaf> result = new RecursiveTree<>();

        for(TreeEntry<TNode, TLeaf> entry : getEntries())
            result.setAt(entry.getPath().reversed(), entry.getItem());

        return result;
    }

    @Override
    public int size()
    {
        final int[] result = {0};
        forEachItem(x -> result[0]++);
        return result[0];
    }

    @Override
    public int countDepth()
    {
        int maxDepth = 0;

        for(TreePath<TNode> path : getPaths())
            if(path.size() > maxDepth)
                maxDepth = path.size();

        return maxDepth;
    }
    //endregion

    //region Iterable implementation
    @Override
    public Iterator<TreeEntry<TNode, TLeaf>> iterator()
    {
        return new Iterator<TreeEntry<TNode, TLeaf>>()
        {
            private final Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack;

            private TreeEntry<TNode, TLeaf> nextResult;

            {
                branchStack = new ArrayDeque<>();
                branchStack.add(new TreeBranchWithPath<>(RecursiveTree.this, TreePath.root()));
                nextResult = progress();
            }

            @Override
            public boolean hasNext()
            { return nextResult != null; }

            @Override
            public TreeEntry<TNode, TLeaf> next()
            {
                TreeEntry<TNode, TLeaf> result = nextResult;

                if(result == null)
                    throw new NoSuchElementException();

                nextResult = progress();
                return result;
            }

            TreeEntry<TNode, TLeaf> progress()
            {
                while(!branchStack.isEmpty())
                {
                    TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
                    RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
                    TreePath<TNode> path = branchWithPath.getPath();

                    for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                        branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

                    if(branch.hasRootItem)
                        return new TreeEntry<>(RecursiveTree.this, path, branch.rootItem);
                }

                return null;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super TreeEntry<TNode, TLeaf>> action)
    { forEachItemWithPath((path, item) -> action.accept(new TreeEntry<>(this, path, item))); }
    //endregion
    //endregion
}
