package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RecursiveTree<TNode, TLeaf> implements Tree<TNode, TLeaf>
{
    protected TLeaf rootItem = null;

    protected boolean hasRootItem = false;

    protected final Map<TNode, RecursiveTree<TNode, TLeaf>> branches = new HashMap<>();

    public RecursiveTree()
    {}

    public RecursiveTree(Tree<TNode, TLeaf> other)
    {
        for(TreeEntry<TNode, TLeaf> entry : other.getEntries())
            setAt(entry.getPath(), entry.getItem());
    }

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

    void trim()
    {
        // TO DO: Write
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean hasItems()
    {
        if(hasRootItem)
            return true;

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            if(branch.hasItems())
                return true;

        return false;
    }

    @Override
    public boolean hasNonRootItems()
    {
        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            if(branch.hasItems())
                return true;

        return false;
    }

    @Override
    public boolean hasItemsAlong(TreePath<TNode> path)
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

        if(branch.hasRootItem)
            return true;

        return false;
    }

    @Override
    public boolean hasNonRootItemsAlong(TreePath<TNode> path)
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
        RecursiveTree<TNode, TLeaf> branch = this;

        for(TNode node : path.getNodes())
        {
            branch = branch.branches.get(node);

            if(branch == null)
                return false;
        }

        return branch.hasRootItem;
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
        RecursiveTree<TNode, TLeaf> branch = this;

        for(TNode node : path.getNodes())
        {
            branch = branch.branches.get(node);

            if(branch == null)
                throw new NoItemAtPathException(this, path);
        }

        if(branch.isEmptyAtRoot())
            throw new NoItemAtPathException(this, path);

        return branch.rootItem;
    }

    @Override
    public TLeaf getAtOr(TreePath<TNode> path, TLeaf defaultItem)
    {
        RecursiveTree<TNode, TLeaf> branch = this;

        for(TNode node : path.getNodes())
        {
            branch = branch.branches.get(node);

            if(branch == null)
                return defaultItem;
        }

        if(branch.isEmptyAtRoot())
            return defaultItem;

        return branch.rootItem;
    }

    @Override
    public Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem)
    {
        RecursiveTree<TNode, TLeaf> branch = this;

        for(TNode node : path.getNodes())
        {
            branch = branch.branches.get(node);

            if(branch == null)
                return defaultItem;
        }

        if(branch.isEmptyAtRoot())
            return defaultItem;

        return branch.rootItem;
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
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem && test.test(path, branch.rootItem))
                result.add(branch.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItemsWherePath(Predicate<? super TreePath<TNode>> test)
    {
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem && test.test(path))
                result.add(branch.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator,
                                                  BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(x ->
            {
                branchStack.add(new TreeBranchWithPath<>(x.getValue(), path.appendedWith(x.getKey())));
            });

            if(branch.hasRootItem && test.test(path, branch.rootItem))
                result.add(branch.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator,
                                                      Predicate<? super TreePath<TNode>> test)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(x ->
            {
                branchStack.add(new TreeBranchWithPath<>(x.getValue(), path.appendedWith(x.getKey())));
            });

            if(branch.hasRootItem && test.test(path))
                result.add(branch.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItemsUnderRoot()
    {
        Deque<RecursiveTree<TNode, TLeaf>> branchStack = new ArrayDeque<>(this.branches.values());
        Collection<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> current = branchStack.remove();
            branchStack.addAll(current.branches.values());

            if(current.hasRootItem)
                result.add(current.rootItem);
        }

        return result;
    }

    @Override
    public List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<RecursiveTree<TNode, TLeaf>> branchStack = new ArrayDeque<>();

        branches.entrySet()
                .stream()
                .sorted(mapEntryComparator)
                .forEachOrdered(x -> branchStack.add(x.getValue()));

        List<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> branch = branchStack.removeLast();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(x -> branchStack.add(x.getValue()));

            if(branch.hasRootItem)
                result.add(branch.rootItem);
        }

        return result;
    }

    @Override
    public List<TLeaf> getItemsAlong(TreePath<TNode> path)
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
    public List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path)
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
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem)
                result.add(new TreeEntry<>(this, path, branch.rootItem));
        }

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(entry ->
            {
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));
            });

            if(branch.hasRootItem)
                result.add(new TreeEntry<>(this, path, branch.rootItem));
        }

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem)
            {
                TreeEntry<TNode, TLeaf> treeEntry = new TreeEntry<>(this, path, branch.rootItem);

                if(test.test(treeEntry))
                    result.add(treeEntry);
            }
        }

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator,
                                                                Predicate<? super TreeEntry<TNode, TLeaf>> test)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(entry ->
            {
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));
            });

            if(branch.hasRootItem)
            {
                TreeEntry<TNode, TLeaf> treeEntry = new TreeEntry<>(this, path, branch.rootItem);

                if(test.test(treeEntry))
                    result.add(treeEntry);
            }
        }

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<? super TreePath<TNode>> test)
    {
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem && test.test(path))
                result.add(new TreeEntry<>(this, path, branch.rootItem));
        }

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator,
                                                                    Predicate<? super TreePath<TNode>> test)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(entry ->
            {
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));
            });

            if(branch.hasRootItem && test.test(path))
                result.add(new TreeEntry<>(this, path, branch.rootItem));
        }

        return result;
    }

    @Override
    public Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot()
    {
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
            branchStack.add(new TreeBranchWithPath<>(entry.getValue(), new TreePath<>(entry.getKey())));

        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem)
                result.add(new TreeEntry<>(this, path, branch.rootItem));
        }

        return result;
    }

    @Override
    public List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
            branchStack.add(new TreeBranchWithPath<>(entry.getValue(), new TreePath<>(entry.getKey())));

        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(entry ->
            {
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));
            });

            if(branch.hasRootItem)
                result.add(new TreeEntry<>(this, path, branch.rootItem));
        }

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
        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        Collection<TreePath<TNode>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branch.branches.entrySet())
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));

            if(branch.hasRootItem)
                result.add(path);
        }

        return result;
    }

    @Override
    public List<TreePath<TNode>> getPathsInOrder(Comparator<? super TNode> comparator)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(new TreeBranchWithPath<>(this, TreePath.root()));
        List<TreePath<TNode>> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            TreeBranchWithPath<RecursiveTree<TNode, TLeaf>, TNode, TLeaf> branchWithPath = branchStack.removeLast();
            RecursiveTree<TNode, TLeaf> branch = branchWithPath.getBranch();
            TreePath<TNode> path = branchWithPath.getPath();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(entry ->
            {
                branchStack.add(new TreeBranchWithPath<>(entry.getValue(), path.appendedWith(entry.getKey())));
            });

            if(branch.hasRootItem)
                result.add(path);
        }

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
            clearAt(entry.getPath());

        trim();
    }

    @Override
    public void clearWherePath(Predicate<? super TreePath<TNode>> test)
    {
        Collection<TreePath<TNode>> paths = getPaths();

        for(TreePath<TNode> path : paths)
            if(test.test(path))
                clearAt(path);

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
        Deque<RecursiveTree<TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(this);
        List<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> current = branchStack.remove();
            branchStack.addAll(current.branches.values());

            if(current.hasRootItem)
                result.add(current.rootItem);
        }

        return result;
    }

    @Override
    public List<TLeaf> toOrderedList(Comparator<? super TNode> comparator)
    {
        Comparator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> mapEntryComparator
                = Map.Entry.comparingByKey(comparator.reversed());

        Deque<RecursiveTree<TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(this);
        List<TLeaf> result = new ArrayList<>();

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> branch = branchStack.removeLast();

            branch.branches.entrySet()
                           .stream()
                           .sorted(mapEntryComparator)
                           .forEachOrdered(x -> branchStack.add(x.getValue()));

            if(branch.hasRootItem)
                result.add(branch.rootItem);
        }

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
        Deque<RecursiveTree<TNode, TLeaf>> branchStack = new ArrayDeque<>();
        branchStack.add(this);
        int result = 0;

        while(!branchStack.isEmpty())
        {
            RecursiveTree<TNode, TLeaf> current = branchStack.removeLast();
            branchStack.addAll(current.branches.values());

            if(current.hasRootItem)
                result++;
        }

        return result;
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
}
