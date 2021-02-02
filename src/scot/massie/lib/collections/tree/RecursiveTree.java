package scot.massie.lib.collections.tree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*

TO DO:
 - Rename all internal methods required for recursion to their equivalent interface method name + "_recursive".
 - - Or "_internal" or something else?

 */

public final class RecursiveTree<TNode, TLeaf> implements Tree<TNode, TLeaf>
{
    public static class Entry<TNode, TLeaf> extends Tree.Entry<TNode, TLeaf>
    {
        public Entry(Tree<TNode, TLeaf> sourceTree, Tree<TNode, TLeaf> sourceBranch, TNode[] path, TLeaf item)
        {
            super(sourceTree, path, item);
            this.sourceBranch = sourceBranch;
        }

        public Entry(Tree<TNode, TLeaf> sourceTree, Tree<TNode, TLeaf> sourceBranch, TreePath<TNode> path, TLeaf item)
        {
            super(sourceTree, path, item);
            this.sourceBranch = sourceBranch;
        }

        protected final Tree<TNode, TLeaf> sourceBranch;
    }

    // NOTE: This is mutable to allow the stream to be added to without replacing the entire object. I'm mostly doing
    // this via the Stream.peek method. This is intentionally using it in a non-debugging fashion and is the cleanest
    // and most efficient way to perform this operation. The point at which these operations occur doesn't matter, just
    // as long as they happen to each element, and they happen in the order specified.
    private static final class PathItemPair<TNode, TLeaf>
    {
        public PathItemPair(Stream<TNode> pathAsStream, RecursiveTree<TNode, TLeaf> sourceBranch, TLeaf item)
        {
            this.pathAsStream = pathAsStream;
            this.sourceBranch = sourceBranch;
            this.item = item;
        }

        private Stream<TNode> pathAsStream;
        private final RecursiveTree<TNode, TLeaf> sourceBranch;
        private final TLeaf item;

        public Stream<TNode> getPathAsStream()
        { return pathAsStream; }

        public void setPathAsStream(Stream<TNode> newPath)
        { pathAsStream = newPath; }

        public PathItemPair<TNode, TLeaf> prefixWith(TNode... prefix)
        {
            pathAsStream = Stream.concat(Stream.of(prefix), pathAsStream);
            return this;
        }

        public PathItemPair<TNode, TLeaf> prefixWith(Stream<TNode> prefix)
        {
            pathAsStream = Stream.concat(prefix, pathAsStream);
            return this;
        }

        public PathItemPair<TNode, TLeaf> prefixWith(List<TNode> prefix)
        {
            pathAsStream = Stream.concat(prefix.stream(), pathAsStream);
            return this;
        }

        public TLeaf getItem()
        { return item; }

        public Entry<TNode, TLeaf> toEntry(RecursiveTree<TNode, TLeaf> sourceTree)
        { return new Entry<>(sourceTree, sourceBranch, TreePath.fromStream(pathAsStream), item); }
    }

    TLeaf rootItem = null;
    boolean hasRootItem = false;
    final Map<TNode, RecursiveTree<TNode, TLeaf>> branches = new HashMap<>();

    private Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> getOrderedBranchIterator(Map<TNode, RecursiveTree<TNode, TLeaf>> branches, Comparator<TNode> comparator)
    { return branches.entrySet().stream().sorted((a, b) -> comparator.compare(a.getKey(), b.getKey())).iterator(); }

    @Override
    public boolean hasItems()
    { return hasRootItem || !branches.isEmpty(); }

    @Override
    public boolean hasNonRootItems()
    { return !branches.isEmpty(); }

    @Override
    public boolean hasItemsAtOrUnder(TNode... path)
    { return hasItemsAtOrUnder_internal(path, 0); }

    public boolean hasItemsAtOrUnder(List<TNode> path)
    { return hasItemsAtOrUnder_internal(path, 0); }

    private boolean hasItemsAtOrUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return hasRootItem || !branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch != null && branch.hasItemsAtOrUnder_internal(path, index + 1);
    }

    private boolean hasItemsAtOrUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return hasRootItem || !branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch != null && branch.hasItemsAtOrUnder_internal(path, index + 1);
    }

    @Override
    public boolean hasItemsUnder(TNode... path)
    { return hasItemsUnder_internal(path, 0); }

    @Override
    public boolean hasItemsUnder(List<TNode> path)
    { return hasItemsUnder_internal(path, 0); }

    private boolean hasItemsUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return !branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch != null && branch.hasItemsUnder_internal(path, index + 1);
    }

    private boolean hasItemsUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return !branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch != null && branch.hasItemsUnder_internal(path, index + 1);
    }

    @Override
    public boolean hasItemsAlong(TNode... path)
    { return hasItemsAlong_internal(path, 0); }

    @Override
    public boolean hasItemsAlong(List<TNode> path)
    { return hasItemsAlong_internal(path, 0); }

    private boolean hasItemsAlong_internal(TNode[] path, int index)
    {
        if(hasRootItem)
            return true;

        if(path.length == index)
            return false;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch != null && branch.hasItemsAlong_internal(path, index + 1);
    }

    private boolean hasItemsAlong_internal(List<TNode> path, int index)
    {
        if(hasRootItem)
            return true;

        if(path.size() == index)
            return false;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch != null && branch.hasItemsAlong_internal(path, index + 1);
    }

    @Override
    public boolean hasNonRootItemsAlong(TNode... path)
    { return hasNonRootItemsAlong_internal(path, 0); }

    @Override
    public boolean hasNonRootItemsAlong(List<TNode> path)
    { return hasNonRootItemsAlong_internal(path, 0); }

    private boolean hasNonRootItemsAlong_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return false;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch != null && branch.hasItemsAlong_internal(path, index + 1);
    }

    private boolean hasNonRootItemsAlong_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return false;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch != null && branch.hasItemsAlong_internal(path, index + 1);
    }

    @Override
    public boolean hasItemAt(TNode... path)
    { return hasItemAt_internal(path, 0); }

    @Override
    public boolean hasItemAt(List<TNode> path)
    { return hasItemAt_internal(path, 0); }

    private boolean hasItemAt_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return hasRootItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch != null && branch.hasItemAt_internal(path, index + 1);
    }

    private boolean hasItemAt_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return hasRootItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch != null && branch.hasItemAt_internal(path, index + 1);
    }

    @Override
    public boolean hasRootItem()
    { return hasRootItem; }

    @Override
    public boolean isEmpty()
    { return (!hasRootItem) && branches.isEmpty(); }

    @Override
    public boolean isEmptyUnderRoot()
    { return branches.isEmpty(); }

    @Override
    public boolean isEmptyAtAndUnder(TNode... path)
    { return isEmptyAtAndUnder_internal(path, 0); }

    @Override
    public boolean isEmptyAtAndUnder(List<TNode> path)
    { return isEmptyAtAndUnder_internal(path, 0); }

    private boolean isEmptyAtAndUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return (!hasRootItem) && branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null || branch.isEmptyAtAndUnder_internal(path, index + 1);
    }

    private boolean isEmptyAtAndUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return (!hasRootItem) && branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null || branch.isEmptyAtAndUnder_internal(path, index + 1);
    }

    @Override
    public boolean isEmptyUnder(TNode... path)
    { return isEmptyUnder_internal(path, 0); }

    @Override
    public boolean isEmptyUnder(List<TNode> path)
    { return isEmptyUnder_internal(path, 0); }

    private boolean isEmptyUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null || branch.isEmptyUnder_internal(path, index + 1);
    }

    private boolean isEmptyUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return branches.isEmpty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null || branch.isEmptyUnder_internal(path, index + 1);
    }

    @Override
    public boolean isEmptyAlong(TNode... path)
    { return isEmptyAlong_internal(path, 0); }

    @Override
    public boolean isEmptyAlong(List<TNode> path)
    { return isEmptyAlong_internal(path, 0); }

    private boolean isEmptyAlong_internal(TNode[] path, int index)
    {
        if(hasRootItem)
            return false;

        if(path.length == index)
            return true;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null || branch.isEmptyAlong_internal(path, index + 1);
    }

    private boolean isEmptyAlong_internal(List<TNode> path, int index)
    {
        if(hasRootItem)
            return false;

        if(path.size() == index)
            return true;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null || branch.isEmptyAlong_internal(path, index + 1);
    }

    @Override
    public boolean isEmptyAlongAfterRoot(TNode... path)
    { return isEmptyAlongAfterRoot_internal(path, 0); }

    @Override
    public boolean isEmptyAlongAfterRoot(List<TNode> path)
    { return isEmptyAlongAfterRoot_internal(path, 0); }

    private boolean isEmptyAlongAfterRoot_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return true;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null || branch.isEmptyAlong_internal(path, index + 1);
    }

    private boolean isEmptyAlongAfterRoot_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return true;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null || branch.isEmptyAlong_internal(path, index + 1);
    }

    @Override
    public boolean isEmptyAt(TNode... path)
    { return isEmptyAt_internal(path, 0); }

    @Override
    public boolean isEmptyAt(List<TNode> path)
    { return isEmptyAt_internal(path, 0); }

    private boolean isEmptyAt_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return !hasRootItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null || branch.isEmptyAt_internal(path, index + 1);
    }

    private boolean isEmptyAt_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return !hasRootItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null || branch.isEmptyAt_internal(path, index + 1);
    }

    @Override
    public boolean isEmptyAtRoot()
    { return !hasRootItem; }

    @Override
    public TLeaf getRootItem() throws NoItemAtPathException
    {
        if(!hasRootItem)
            throw new NoItemAtPathException();

        return rootItem;
    }

    @Override
    public ValueWithPresence<TLeaf> getRootItemSafely()
    { return new ValueWithPresence<>(hasRootItem, rootItem); }

    @Override
    public TLeaf getRootItemOrDefault(TLeaf defaultItem)
    { return hasRootItem ? rootItem : defaultItem; }

    @Override
    public Object getRootItemOrDefaultAnyType(Object defaultItem)
    { return hasRootItem ? rootItem : defaultItem; }

    @Override
    public TLeaf getRootItemOrNull()
    { return hasRootItem ? rootItem : null; }

    @Override
    public TLeaf getAt(TNode... path) throws NoItemAtPathException
    { return getAt_internal(path, 0); }

    @Override
    public TLeaf getAt(List<TNode> path) throws NoItemAtPathException
    { return getAt_internal(path, 0); }

    private TLeaf getAt_internal(TNode[] path, int index) throws NoItemAtPathException
    {
        if(path.length == index)
        {
            if(!hasRootItem)
                throw new NoItemAtPathException();

            return rootItem;
        }

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);

        if(branch == null)
            throw new NoItemAtPathException();

        return branch.getAt_internal(path, index + 1);
    }

    private TLeaf getAt_internal(List<TNode> path, int index) throws NoItemAtPathException
    {
        if(path.size() == index)
        {
            if(!hasRootItem)
                throw new NoItemAtPathException();

            return rootItem;
        }

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);

        if(branch == null)
            throw new NoItemAtPathException();

        return branch.getAt_internal(path, index + 1);
    }

    @Override
    public ValueWithPresence<TLeaf> getAtSafely(TNode... path)
    { return getAtSafely_internal(path, 0); }

    @Override
    public ValueWithPresence<TLeaf> getAtSafely(List<TNode> path)
    { return getAtSafely_internal(path, 0); }

    private ValueWithPresence<TLeaf> getAtSafely_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return new ValueWithPresence<>(hasRootItem, rootItem);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? new ValueWithPresence<>(false, null) : branch.getAtSafely_internal(path, index + 1);
    }

    private ValueWithPresence<TLeaf> getAtSafely_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return new ValueWithPresence<>(hasRootItem, rootItem);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? new ValueWithPresence<>(false, null) : branch.getAtSafely_internal(path, index + 1);
    }

    @Override
    public TLeaf getAtOrDefault(TLeaf defaultItem, TNode... path)
    { return getAtOrDefault_internal(defaultItem, path, 0); }

    @Override
    public TLeaf getAtOrDefault(TLeaf defaultItem, List<TNode> path)
    { return getAtOrDefault_internal(defaultItem, path, 0); }

    private TLeaf getAtOrDefault_internal(TLeaf defaultItem, TNode[] path, int index)
    {
        if(path.length == index)
            return hasRootItem ? rootItem : defaultItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? defaultItem : branch.getAtOrDefault_internal(defaultItem, path, index + 1);
    }

    private TLeaf getAtOrDefault_internal(TLeaf defaultItem, List<TNode> path, int index)
    {
        if(path.size() == index)
            return hasRootItem ? rootItem : defaultItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? defaultItem : branch.getAtOrDefault_internal(defaultItem, path, index + 1);
    }

    @Override
    public Object getAtOrDefaultAnyType(Object defaultItem, TNode... path)
    { return getAtOrDefaultAnyType_internal(defaultItem, path, 0); }

    @Override
    public Object getAtOrDefaultAnyType(Object defaultItem, List<TNode> path)
    { return getAtOrDefaultAnyType_internal(defaultItem, path, 0); }

    private Object getAtOrDefaultAnyType_internal(Object defaultItem, TNode[] path, int index)
    {
        if(path.length == index)
            return hasRootItem ? rootItem : defaultItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? defaultItem : branch.getAtOrDefaultAnyType_internal(defaultItem, path, index + 1);
    }

    private Object getAtOrDefaultAnyType_internal(Object defaultItem, List<TNode> path, int index)
    {
        if(path.size() == index)
            return hasRootItem ? rootItem : defaultItem;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? defaultItem : branch.getAtOrDefaultAnyType_internal(defaultItem, path, index + 1);
    }

    @Override
    public TLeaf getAtOrNull(TNode... path)
    { return getAtOrNull_internal(path, 0); }

    @Override
    public TLeaf getAtOrNull(List<TNode> path)
    { return getAtOrNull_internal(path, 0); }

    private TLeaf getAtOrNull_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return hasRootItem ? rootItem : null;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? null : branch.getAtOrNull_internal(path, index + 1);
    }

    private TLeaf getAtOrNull_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return hasRootItem ? rootItem : null;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? null : branch.getAtOrNull_internal(path, index + 1);
    }

    @Override
    public Collection<Tree.TreePath<TNode>> getPaths()
    { return getPaths_internal().map(TreePath::fromStream).collect(Collectors.toList()); }

    private Stream<Stream<TNode>> getPaths_internal()
    {
        Stream<Stream<TNode>> result = hasRootItem ? Stream.of(Stream.empty()) : null;

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
        {
            TNode node = branchEntry.getKey();

            Stream<Stream<TNode>> branchPathsStreamStream = branchEntry
                    .getValue()
                    .getPaths_internal()
                    .map(x -> Stream.concat(Stream.of(node), x));

            result = result != null
                             ? Stream.concat(result, branchPathsStreamStream)
                             : branchPathsStreamStream;
        }

        return result != null ? result : Stream.empty();
    }

    @Override
    public List<Tree.TreePath<TNode>> getPathsInOrder(Comparator<TNode> comparator)
    { return getPathsInOrder_internal(comparator).map(TreePath::fromStream).collect(Collectors.toList()); }

    private Stream<Stream<TNode>> getPathsInOrder_internal(Comparator<TNode> comparator)
    {
        Stream<Stream<TNode>> result = hasRootItem ? Stream.of(Stream.empty()) : Stream.empty();
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> branchIterator;

        branchIterator = branches.entrySet()
                                 .stream()
                                 .sorted((a, b) -> comparator.compare(a.getKey(), b.getKey()))
                                 .iterator();

        while(branchIterator.hasNext())
        {
            Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> currentEntry = branchIterator.next();
            TNode node = currentEntry.getKey();

            Stream<Stream<TNode>> branchPathsStreamStream = currentEntry
                    .getValue()
                    .getPathsInOrder_internal(comparator)
                    .map(x -> Stream.concat(Stream.of(node), x));

            result = Stream.concat(result, branchPathsStreamStream);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItems()
    { return getItemsAsStream().collect(Collectors.toList()); }

    private Stream<TLeaf> getItemsAsStream()
    {
        Stream<TLeaf> result = hasRootItem ? Stream.of(rootItem) : null;

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            result = result == null ? branch.getItemsAsStream() : Stream.concat(result, branch.getItemsAsStream());

        return result == null ? Stream.empty() : result;
    }

    @Override
    public List<TLeaf> getItemsInOrder(Comparator<TNode> comparator)
    { return getItemsInOrderAsStream(comparator).collect(Collectors.toList()); }

    private Stream<TLeaf> getItemsInOrderAsStream(Comparator<TNode> comparator)
    {
        Stream<TLeaf> result = hasRootItem ? Stream.of(rootItem) : null;
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> branchEntryIter;

        branchEntryIter = branches.entrySet()
                                  .stream()
                                  .sorted((a, b) -> comparator.compare(a.getKey(), b.getKey()))
                                  .iterator();

        while(branchEntryIter.hasNext())
        {
            Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> currentEntry = branchEntryIter.next();
            Stream<TLeaf> branchItems = currentEntry.getValue().getItemsInOrderAsStream(comparator);
            result = result == null ? branchItems : Stream.concat(result, branchItems);
        }

        return result == null ? Stream.empty() : result;
    }

    @Override
    public Collection<TLeaf> getItemsAtAndUnder(TNode... path)
    { return getItemsAtAndUnder_internal(path, 0); }

    @Override
    public Collection<TLeaf> getItemsAtAndUnder(List<TNode> path)
    { return getItemsAtAndUnder_internal(path, 0); }

    private Collection<TLeaf> getItemsAtAndUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return getItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndUnder_internal(path, index + 1);
    }

    private Collection<TLeaf> getItemsAtAndUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return getItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndUnder_internal(path, index + 1);
    }

    @Override
    public List<TLeaf> getItemsAtAndUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getItemsAtAndUnderInOrder_internal(comparator, path, 0); }

    @Override
    public List<TLeaf> getItemsAtAndUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getItemsAtAndUnderInOrder_internal(comparator, path, 0); }

    private List<TLeaf> getItemsAtAndUnderInOrder_internal(Comparator<TNode> comp, TNode[] path, int index)
    {
        if(path.length == index)
            return getItemsInOrder(comp);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndUnderInOrder_internal(comp, path, index + 1);
    }

    private List<TLeaf> getItemsAtAndUnderInOrder_internal(Comparator<TNode> comp, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getItemsInOrder(comp);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndUnderInOrder_internal(comp, path, index + 1);
    }

    @Override
    public Collection<TLeaf> getNonRootItems()
    {
        Stream<TLeaf> result = null;

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            result = result == null ? branch.getItemsAsStream() : Stream.concat(result, branch.getItemsAsStream());

        return result == null ? new ArrayList<>() : result.collect(Collectors.toList());
    }

    @Override
    public List<TLeaf> getNonRootItemsInOrder(Comparator<TNode> comparator)
    {
        Stream<TLeaf> result = null;
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> branchEntryIter;

        branchEntryIter = branches
                .entrySet()
                .stream()
                .sorted((a, b) -> comparator.compare(a.getKey(), b.getKey()))
                .iterator();

        while(branchEntryIter.hasNext())
        {
            RecursiveTree<TNode, TLeaf> branch = branchEntryIter.next().getValue();
            result = result == null ? branch.getItemsInOrderAsStream(comparator) : Stream.concat(result, branch.getItemsAsStream());
        }

        return result == null ? Collections.emptyList() : result.collect(Collectors.toList());
    }

    @Override
    public Collection<TLeaf> getItemsUnder(TNode... path)
    { return getItemsUnder_internal(path, 0); }

    @Override
    public Collection<TLeaf> getItemsUnder(List<TNode> path)
    { return getItemsUnder_internal(path, 0); }

    private Collection<TLeaf> getItemsUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return getNonRootItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsUnder_internal(path, index + 1);
    }

    private Collection<TLeaf> getItemsUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return getNonRootItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsUnder_internal(path, index + 1);
    }

    @Override
    public List<TLeaf> getItemsUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getItemsUnderInOrder_internal(comparator, path, 0); }

    @Override
    public List<TLeaf> getItemsUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getItemsUnderInOrder_internal(comparator, path, 0); }

    private List<TLeaf> getItemsUnderInOrder_internal(Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getNonRootItemsInOrder(comparator);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsUnderInOrder_internal(comparator, path, index + 1);
    }

    private List<TLeaf> getItemsUnderInOrder_internal(Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getNonRootItemsInOrder(comparator);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsUnderInOrder_internal(comparator, path, index + 1);
    }

    @Override
    public List<TLeaf> getItemsAlong(TNode... path)
    { return getItemsAlong_internal(path, 0).collect(Collectors.toList()); }

    @Override
    public List<TLeaf> getItemsAlong(List<TNode> path)
    { return getItemsAlong_internal(path, 0).collect(Collectors.toList()); }

    private Stream<TLeaf> getItemsAlong_internal(TNode[] path, int index)
    {
        Stream<TLeaf> result = hasRootItem ? Stream.of(rootItem) : null;

        if(path.length == index)
            return result == null ? Stream.empty() : result;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);

        if(branch == null)
            return result == null ? Stream.empty() : result;
        else
        {
            Stream<TLeaf> branchResult = branch.getItemsAlong_internal(path, index + 1);
            return result == null ? branchResult : Stream.concat(result, branchResult);
        }
    }

    private Stream<TLeaf> getItemsAlong_internal(List<TNode> path, int index)
    {
        Stream<TLeaf> result = hasRootItem ? Stream.of(rootItem) : null;

        if(path.size() == index)
            return result == null ? Stream.empty() : result;

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);

        if(branch == null)
            return result == null ? Stream.empty() : result;
        else
        {
            Stream<TLeaf> branchResult = branch.getItemsAlong_internal(path, index + 1);
            return result == null ? branchResult : Stream.concat(result, branchResult);
        }
    }

    @Override
    public List<TLeaf> getNonRootItemsAlong(TNode... path)
    { return getNonRootItemsAlong_internal(path, 0).collect(Collectors.toList()); }

    @Override
    public List<TLeaf> getNonRootItemsAlong(List<TNode> path)
    { return getNonRootItemsAlong_internal(path, 0).collect(Collectors.toList()); }

    private Stream<TLeaf> getNonRootItemsAlong_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return Stream.empty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Stream.empty() : branch.getItemsAlong_internal(path, index + 1);
    }

    private Stream<TLeaf> getNonRootItemsAlong_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return Stream.empty();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Stream.empty() : branch.getItemsAlong_internal(path, index + 1);
    }

    @Override
    public Collection<TLeaf> getImmediateItems()
    {
        List<TLeaf> result = new ArrayList<>();

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            if(branch.hasRootItem)
                result.add(branch.rootItem);

        return result;
    }

    @Override
    public List<TLeaf> getImmediateItemsInOrder(Comparator<TNode> comparator)
    {
        List<TLeaf> result = new ArrayList<>();
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> branchEntryIter;
        branchEntryIter = branches
                .entrySet()
                .stream()
                .sorted((a, b) -> comparator.compare(a.getKey(), b.getKey()))
                .iterator();

        while(branchEntryIter.hasNext())
        {
            RecursiveTree<TNode, TLeaf> currentBranch = branchEntryIter.next().getValue();

            if(currentBranch.hasRootItem)
                result.add(currentBranch.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItemsImmediatelyUnder(TNode... path)
    { return getItemsImmediatelyUnder_internal(path, 0); }

    @Override
    public Collection<TLeaf> getItemsImmediatelyUnder(List<TNode> path)
    { return getItemsImmediatelyUnder_internal(path, 0); }

    private Collection<TLeaf> getItemsImmediatelyUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return getImmediateItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsImmediatelyUnder_internal(path, index + 1);
    }

    private Collection<TLeaf> getItemsImmediatelyUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return getImmediateItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsImmediatelyUnder_internal(path, index + 1);
    }

    @Override
    public List<TLeaf> getItemsImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getItemsImmediatelyUnderInOrder_internal(comparator, path, 0); }

    @Override
    public List<TLeaf> getItemsImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getItemsImmediatelyUnderInOrder_internal(comparator, path, 0); }

    private List<TLeaf> getItemsImmediatelyUnderInOrder_internal(Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getImmediateItemsInOrder(comparator);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsImmediatelyUnderInOrder_internal(comparator, path, index + 1);
    }

    private List<TLeaf> getItemsImmediatelyUnderInOrder_internal(Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getImmediateItemsInOrder(comparator);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsImmediatelyUnderInOrder_internal(comparator, path, index + 1);
    }

    @Override
    public Collection<TLeaf> getRootAndImmediateItems()
    {
        if(!hasRootItem)
            return getImmediateItems();

        List<TLeaf> result = new ArrayList<>();
        result.add(rootItem);

        for(RecursiveTree<TNode, TLeaf> branch : branches.values())
            if(branch.hasRootItem)
                result.add(branch.rootItem);

        return result;
    }

    @Override
    public List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<TNode> comparator)
    {
        if(!hasRootItem)
            return getImmediateItemsInOrder(comparator);

        List<TLeaf> result = new ArrayList<>();
        result.add(rootItem);
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> branchEntryIter;
        branchEntryIter = branches
                .entrySet()
                .stream()
                .sorted((a, b) -> comparator.compare(a.getKey(), b.getKey()))
                .iterator();

        while(branchEntryIter.hasNext())
        {
            RecursiveTree<TNode, TLeaf> current = branchEntryIter.next().getValue();

            if(current.hasRootItem)
                result.add(current.rootItem);
        }

        return result;
    }

    @Override
    public Collection<TLeaf> getItemsAtAndImmediatelyUnder(TNode... path)
    { return getItemsAtAndImmediatelyUnder_internal(path, 0); }

    @Override
    public Collection<TLeaf> getItemsAtAndImmediatelyUnder(List<TNode> path)
    { return getItemsAtAndImmediatelyUnder_internal(path, 0); }

    private Collection<TLeaf> getItemsAtAndImmediatelyUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return getRootAndImmediateItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndImmediatelyUnder_internal(path, index + 1);
    }

    private Collection<TLeaf> getItemsAtAndImmediatelyUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return getRootAndImmediateItems();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndImmediatelyUnder_internal(path, index + 1);
    }

    @Override
    public List<TLeaf> getItemsAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getItemsAtAndImmediatelyUnderInOrder_internal(comparator, path, 0); }

    @Override
    public List<TLeaf> getItemsAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getItemsAtAndImmediatelyUnderInOrder_internal(comparator, path, 0); }

    private List<TLeaf> getItemsAtAndImmediatelyUnderInOrder_internal(Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getRootAndImmediateItemsInOrder(comparator);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndImmediatelyUnderInOrder_internal(comparator, path, index + 1);
    }

    private List<TLeaf> getItemsAtAndImmediatelyUnderInOrder_internal(Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getRootAndImmediateItemsInOrder(comparator);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getItemsAtAndImmediatelyUnderInOrder_internal(comparator, path, index + 1);
    }

    private Stream<PathItemPair<TNode, TLeaf>> getPathsAndItemsAsStream()
    {
        Stream<PathItemPair<TNode, TLeaf>> result = hasRootItem
                ? Stream.of(new PathItemPair<>(Stream.empty(), this, rootItem))
                : null;

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
        {
            TNode node = branchEntry.getKey();
            Stream<PathItemPair<TNode, TLeaf>> branchPathItemStream = branchEntry.getValue()
                                                                                 .getPathsAndItemsAsStream()
                                                                                 .peek(x -> x.prefixWith(node));

            result = result != null
                    ? Stream.concat(result, branchPathItemStream)
                    : branchPathItemStream;
        }

        return result != null ? result : Stream.empty();
    }

    private Stream<PathItemPair<TNode, TLeaf>> getPathsAndItemsAsStreamInOrder(Comparator<TNode> comparator)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = hasRootItem
                ? Stream.of(new PathItemPair<>(Stream.empty(), this, rootItem))
                : null;

        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> branchEntryIter;
        branchEntryIter = getOrderedBranchIterator(branches, comparator);

        while(branchEntryIter.hasNext())
        {
            Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry = branchEntryIter.next();
            TNode node = branchEntry.getKey();
            Stream<PathItemPair<TNode, TLeaf>> branchPathItemStream
                    = branchEntry.getValue().getPathsAndItemsAsStreamInOrder(comparator).peek(x -> x.prefixWith(node));

            result = result != null
                    ? Stream.concat(result, branchPathItemStream)
                    : branchPathItemStream;
        }

        return result != null ? result : Stream.empty();
    }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesAsStream()
    { return getPathsAndItemsAsStream().map(x -> x.toEntry(this)); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesAsStreamInOrder(Comparator<TNode> comparator)
    { return getPathsAndItemsAsStreamInOrder(comparator).map(x -> x.toEntry(this)); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesWithPathPrefixedAsStream(RecursiveTree<TNode, TLeaf> sourceTree, List<TNode> prefix)
    { return getPathsAndItemsAsStream().peek(x -> x.prefixWith(prefix)).map(x -> x.toEntry(sourceTree)); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesWithPathPrefixedAsStreamInOrder(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, List<TNode> prefix)
    { return getPathsAndItemsAsStreamInOrder(comparator).peek(x -> x.prefixWith(prefix)).map(x -> x.toEntry(sourceTree)); }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntries()
    { return getEntriesAsStream().collect(Collectors.toList()); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesInOrder(Comparator<TNode> comparator)
    { return getEntriesAsStreamInOrder(comparator).collect(Collectors.toList()); }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder(TNode... path)
    { return getEntriesAtAndUnder_internal(this, path, 0).collect(Collectors.toList()); }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder(List<TNode> path)
    { return getEntriesAtAndUnder_internal(this, path, 0).collect(Collectors.toList()); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, TNode[] path, int index)
    {
        if(path.length == index)
            return getEntriesWithPathPrefixedAsStream(sourceTree, Arrays.asList(path));

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Stream.empty() : branch.getEntriesAtAndUnder_internal(sourceTree, path, index + 1);
    }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getEntriesWithPathPrefixedAsStream(sourceTree, path);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Stream.empty() : branch.getEntriesAtAndUnder_internal(sourceTree, path, index + 1);
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getEntriesAtAndUnderInOrder_internal(this, comparator, path, 0).collect(Collectors.toList()); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getEntriesAtAndUnderInOrder_internal(this, comparator, path, 0).collect(Collectors.toList()); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getEntriesWithPathPrefixedAsStreamInOrder(sourceTree, comparator, Arrays.asList(path));

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Stream.empty() : branch.getEntriesAtAndUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesAtAndUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getEntriesWithPathPrefixedAsStreamInOrder(sourceTree, comparator, path);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Stream.empty() : branch.getEntriesAtAndUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getNonRootEntries()
    { return getNonRootPathsAndItems().map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getNonRootPathsAndItems(List<TNode> prefix)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = null;

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
        {
            Stream<PathItemPair<TNode, TLeaf>> branchContents = branchEntry.getValue()
                                                                           .getPathsAndItemsAsStream()
                                                                           .peek(x -> x.prefixWith(branchEntry.getKey())
                                                                                       .prefixWith(prefix));
            result = result != null ? Stream.concat(result, branchContents) : branchContents;
        }

        return result != null ? result : Stream.empty();
    }

    private Stream<PathItemPair<TNode, TLeaf>> getNonRootPathsAndItems()
    {
        Stream<PathItemPair<TNode, TLeaf>> result = null;

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
        {
            Stream<PathItemPair<TNode, TLeaf>> branchContents = branchEntry.getValue()
                                                                           .getPathsAndItemsAsStream()
                                                                           .peek(x -> x.prefixWith(branchEntry.getKey()));
            result = result != null ? Stream.concat(result, branchContents) : branchContents;
        }

        return result != null ? result : Stream.empty();
    }

    private Stream<Tree.Entry<TNode, TLeaf>> getNonRootEntriesAsStream(RecursiveTree<TNode, TLeaf> sourceTree, List<TNode> prefix)
    { return getNonRootPathsAndItems(prefix).map(x -> x.toEntry(sourceTree)); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesInOrder(Comparator<TNode> comparator)
    { return getNonRootPathsAndItemsInOrder(comparator).map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getNonRootPathsAndItemsInOrder(Comparator<TNode> comparator)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = null;
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> iter = getOrderedBranchIterator(branches, comparator);

        while(iter.hasNext())
        {
            Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry = iter.next();
            Stream<PathItemPair<TNode, TLeaf>> pathsAndItemsStream = branchEntry
                    .getValue()
                    .getPathsAndItemsAsStreamInOrder(comparator)
                    .peek(x -> x.prefixWith(branchEntry.getKey()));

            result = result != null ? Stream.concat(result, pathsAndItemsStream) : pathsAndItemsStream;
        }

        return result != null ? result : Stream.empty();
    }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesUnder(TNode... path)
    { return getEntriesUnder_internal(this, path, 0).collect(Collectors.toList()); }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesUnder(List<TNode> path)
    { return getEntriesUnder_internal(this, path, 0).collect(Collectors.toList()); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, TNode[] path, int index)
    {
        if(path.length == index)
            return getNonRootPathsAndItems().peek(x -> x.prefixWith(path)).map(x -> x.toEntry(sourceTree));

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Stream.empty() : branch.getEntriesUnder_internal(sourceTree, path, index + 1);
    }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getNonRootPathsAndItems().peek(x -> x.prefixWith(path)).map(x -> x.toEntry(sourceTree));

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Stream.empty() : branch.getEntriesUnder_internal(sourceTree, path, index + 1);
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getEntriesUnderInOrder_internal(this, comparator, path, 0).collect(Collectors.toList()); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getEntriesUnderInOrder_internal(this, comparator, path, 0).collect(Collectors.toList()); }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getNonRootPathsAndItemsInOrder(comparator).peek(x -> x.prefixWith(path)).map(x -> x.toEntry(sourceTree));

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Stream.empty() : branch.getEntriesUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    private Stream<Tree.Entry<TNode, TLeaf>> getEntriesUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getNonRootPathsAndItemsInOrder(comparator).peek(x -> x.prefixWith(path)).map(x -> x.toEntry(sourceTree));

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Stream.empty() : branch.getEntriesUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesAlong(TNode... path)
    { return getPathsAndItemsAlong(path).map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesAlong(List<TNode> path)
    { return getPathsAndItemsAlong(path).map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getPathsAndItemsAlong(TNode[] path)
    { return getPathsAndItemsAlong(path, 0); }

    private Stream<PathItemPair<TNode, TLeaf>> getPathsAndItemsAlong(List<TNode> path)
    { return getPathsAndItemsAlong(path, 0); }

    private Stream<PathItemPair<TNode, TLeaf>> getPathsAndItemsAlong(TNode[] path, int index)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = hasRootItem
                ? Stream.of(new PathItemPair<>(Stream.empty(), this, rootItem))
                : null;

        if(index == path.length)
            return result != null ? result : Stream.empty();

        TNode node = path[index];
        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(node, null);

        if(branch == null)
            return result != null ? result : Stream.empty();

        Stream<PathItemPair<TNode, TLeaf>> branchResult = branch.getPathsAndItemsAlong(path, index + 1)
                                                                .peek(x -> x.prefixWith(node));

        return result != null ? Stream.concat(result, branchResult) : branchResult;
    }

    private Stream<PathItemPair<TNode, TLeaf>> getPathsAndItemsAlong(List<TNode> path, int index)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = hasRootItem
                                                            ? Stream.of(new PathItemPair<>(Stream.empty(), this, rootItem))
                                                            : null;

        if(index == path.size())
            return result != null ? result : Stream.empty();

        TNode node = path.get(index);
        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(node, null);

        if(branch == null)
            return result != null ? result : Stream.empty();

        Stream<PathItemPair<TNode, TLeaf>> branchResult = branch.getPathsAndItemsAlong(path, index + 1)
                                                                .peek(x -> x.prefixWith(node));

        return result != null ? Stream.concat(result, branchResult) : branchResult;
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesAlong(TNode... path)
    {
        if(path.length == 0)
            return new ArrayList<>();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[0], null);
        return branch != null
            ? branch.getPathsAndItemsAlong(path, 1)
                    .peek(x -> x.prefixWith(path[0]))
                    .map(x -> x.toEntry(this))
                    .collect(Collectors.toList())
            : new ArrayList<>();
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getNonRootEntriesAlong(List<TNode> path)
    {
        if(path.size() == 0)
            return new ArrayList<>();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(0), null);
        return branch != null
                       ? branch.getPathsAndItemsAlong(path, 1)
                               .peek(x -> x.prefixWith(path.get(0)))
                               .map(x -> x.toEntry(this))
                               .collect(Collectors.toList())
                       : new ArrayList<>();
    }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getImmediateEntries()
    { return getImmediatePathsAndItems().map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getImmediatePathsAndItems()
    {
        Stream<PathItemPair<TNode, TLeaf>> result = Stream.empty();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
        {
            RecursiveTree<TNode, TLeaf> branch = branchEntry.getValue();

            if(branch.hasRootItem)
            {
                PathItemPair<TNode, TLeaf> pathItemPair = new PathItemPair<>(Stream.of(branchEntry.getKey()),
                                                                             branch,
                                                                             branch.rootItem);
                result = Stream.concat(result, Stream.of(pathItemPair));
            }
        }

        return result;
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<TNode> comparator)
    { return getImmediatePathsAndItemsInOrder(comparator).map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getImmediatePathsAndItemsInOrder(Comparator<TNode> comparator)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = Stream.empty();
        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> iter = getOrderedBranchIterator(branches, comparator);

        while(iter.hasNext())
        {
            Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry = iter.next();
            RecursiveTree<TNode, TLeaf> branch = branchEntry.getValue();

            if(branch.hasRootItem)
            {
                PathItemPair<TNode, TLeaf> pathItemPair = new PathItemPair<>(Stream.of(branchEntry.getKey()),
                                                                             branch,
                                                                             branch.rootItem);
                result = Stream.concat(result, Stream.of(pathItemPair));
            }
        }

        return result;
    }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder(TNode... path)
    { return getEntriesImmediatelyUnder_internal(this, path, 0); }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder(List<TNode> path)
    { return getEntriesImmediatelyUnder_internal(this, path, 0); }

    private Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, TNode[] path, int index)
    {
        if(path.length == index)
            return getImmediatePathsAndItems().peek(x -> x.prefixWith(path))
                                              .map(x -> x.toEntry(sourceTree))
                                              .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getEntriesImmediatelyUnder_internal(sourceTree, path, index + 1);
    }

    private Collection<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getImmediatePathsAndItems().peek(x -> x.prefixWith(path))
                                              .map(x -> x.toEntry(sourceTree))
                                              .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getEntriesImmediatelyUnder_internal(sourceTree, path, index + 1);
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getEntriesImmediatelyUnderInOrder_internal(this, comparator, path, 0); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getEntriesImmediatelyUnderInOrder_internal(this, comparator, path, 0); }

    private List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getImmediatePathsAndItemsInOrder(comparator).peek(x -> x.prefixWith(path))
                                                               .map(x -> x.toEntry(sourceTree))
                                                               .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getEntriesImmediatelyUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    private List<Tree.Entry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getImmediatePathsAndItemsInOrder(comparator).peek(x -> x.prefixWith(path))
                                                               .map(x -> x.toEntry(sourceTree))
                                                               .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getEntriesImmediatelyUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getRootAndImmediateEntries()
    { return getRootAndImmediatePathsAndItems().map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getRootAndImmediatePathsAndItems()
    {
        Stream<PathItemPair<TNode, TLeaf>> result = hasRootItem
                ? Stream.of(new PathItemPair<>(Stream.empty(), this, rootItem))
                : Stream.empty();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
        {
            RecursiveTree<TNode, TLeaf> branch = branchEntry.getValue();

            if(branch.hasRootItem)
            {
                PathItemPair<TNode, TLeaf> pathItemPair = new PathItemPair<>(Stream.of(branchEntry.getKey()),
                                                                             branch,
                                                                             branch.rootItem);
                result = Stream.concat(result, Stream.of(pathItemPair));
            }
        }

        return result;
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<TNode> comparator)
    { return getRootAndImmediatePathsAndItemsInOrder(comparator).map(x -> x.toEntry(this)).collect(Collectors.toList()); }

    private Stream<PathItemPair<TNode, TLeaf>> getRootAndImmediatePathsAndItemsInOrder(Comparator<TNode> comparator)
    {
        Stream<PathItemPair<TNode, TLeaf>> result = hasRootItem
                ? Stream.of(new PathItemPair<>(Stream.empty(), this, rootItem))
                : Stream.empty();

        Iterator<Map.Entry<TNode, RecursiveTree<TNode, TLeaf>>> iter = getOrderedBranchIterator(branches, comparator);

        while(iter.hasNext())
        {
            Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry = iter.next();
            RecursiveTree<TNode, TLeaf> branch = branchEntry.getValue();

            if(branch.hasRootItem)
            {
                PathItemPair<TNode, TLeaf> pathItemPair = new PathItemPair<>(Stream.of(branchEntry.getKey()),
                                                                             branch,
                                                                             branch.rootItem);
                result = Stream.concat(result, Stream.of(pathItemPair));
            }
        }

        return result;
    }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(TNode... path)
    { return getEntriesAtAndImmediatelyUnder_internal(this, path, 0); }

    @Override
    public Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(List<TNode> path)
    { return getEntriesAtAndImmediatelyUnder_internal(this, path, 0); }

    private Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, TNode[] path, int index)
    {
        if(path.length == index)
            return getRootAndImmediatePathsAndItems().peek(x -> x.prefixWith(path))
                                                     .map(x -> x.toEntry(sourceTree))
                                                     .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getEntriesAtAndImmediatelyUnder_internal(sourceTree, path, index + 1);
    }

    private Collection<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder_internal(RecursiveTree<TNode, TLeaf> sourceTree, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getRootAndImmediatePathsAndItems().peek(x -> x.prefixWith(path))
                                                     .map(x -> x.toEntry(sourceTree))
                                                     .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getEntriesAtAndImmediatelyUnder_internal(sourceTree, path, index + 1);
    }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, TNode... path)
    { return getEntriesAtAndImmediatelyUnderInOrder_internal(this, comparator, path, 0); }

    @Override
    public List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(Comparator<TNode> comparator, List<TNode> path)
    { return getEntriesAtAndImmediatelyUnderInOrder_internal(this, comparator, path, 0); }

    private List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, TNode[] path, int index)
    {
        if(path.length == index)
            return getRootAndImmediatePathsAndItemsInOrder(comparator).peek(x -> x.prefixWith(path))
                                                                      .map(x -> x.toEntry(sourceTree))
                                                                      .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? Collections.emptyList() : branch.getEntriesAtAndImmediatelyUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    private List<Tree.Entry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder_internal(RecursiveTree<TNode, TLeaf> sourceTree, Comparator<TNode> comparator, List<TNode> path, int index)
    {
        if(path.size() == index)
            return getRootAndImmediatePathsAndItemsInOrder(comparator).peek(x -> x.prefixWith(path))
                                                                      .map(x -> x.toEntry(sourceTree))
                                                                      .collect(Collectors.toList());

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? Collections.emptyList() : branch.getEntriesAtAndImmediatelyUnderInOrder_internal(sourceTree, comparator, path, index + 1);
    }

    @Override
    public RecursiveTree<TNode, TLeaf> getBranch(TNode branchId)
    {
        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(branchId, null);
        return branch == null ? null : branch.copy();
    }

    @Override
    public RecursiveTree<TNode, TLeaf> getBranch(TNode... path)
    { return getBranch_internal(path, 0); }

    @Override
    public RecursiveTree<TNode, TLeaf> getBranch(List<TNode> path)
    { return getBranch_internal(path, 0); }

    private RecursiveTree<TNode, TLeaf> getBranch_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return copy();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? new RecursiveTree<>() : branch.getBranch_internal(path, index + 1);
    }

    private RecursiveTree<TNode, TLeaf> getBranch_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return copy();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? new RecursiveTree<>() : branch.getBranch_internal(path, index + 1);
    }

    @Override
    public Collection<Tree<TNode, TLeaf>> getBranches()
    { return branches.values().stream().map(x -> x.copy()).collect(Collectors.toList()); }

    @Override
    public Collection<Tree<TNode, TLeaf>> getBranches(TNode... path)
    { return getBranches_internal(path, 0); }

    @Override
    public Collection<Tree<TNode, TLeaf>> getBranches(List<TNode> path)
    { return getBranches_internal(path, 0); }

    private Collection<Tree<TNode, TLeaf>> getBranches_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return getBranches();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? null : branch.getBranches_internal(path, index + 1);
    }

    private Collection<Tree<TNode, TLeaf>> getBranches_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return getBranches();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? null : branch.getBranches_internal(path, index + 1);
    }

    @Override
    public Map<TNode, RecursiveTree<TNode, TLeaf>> getBranchesWithFirstPathFragments()
    {
        Map<TNode, RecursiveTree<TNode, TLeaf>> result = new HashMap<>();

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> entry : branches.entrySet())
            result.put(entry.getKey(), entry.getValue().copy());

        return result;
    }

    @Override
    public ValueWithPresence<TLeaf> setRootItem(TLeaf newItem)
    {
        ValueWithPresence<TLeaf> r = new ValueWithPresence<>(hasRootItem, rootItem);
        rootItem = newItem;
        hasRootItem = true;
        return r;
    }

    @Override
    public ValueWithPresence<TLeaf> setRootItemIfAbsent(TLeaf newItem)
    {
        ValueWithPresence<TLeaf> r = new ValueWithPresence<>(hasRootItem, rootItem);

        if(!hasRootItem)
        {
            rootItem = newItem;
            hasRootItem = true;
        }

        return r;
    }

    @Override
    public ValueWithPresence<TLeaf> setAt(TLeaf newItem, TNode... path)
    { return setAt_internal(newItem, path, 0); }

    @Override
    public ValueWithPresence<TLeaf> setAt(TLeaf newItem, List<TNode> path)
    { return setAt_internal(newItem, path, 0); }

    private ValueWithPresence<TLeaf> setAt_internal(TLeaf newItem, TNode[] path, int index)
    {
        if(path.length == index)
            return setRootItem(newItem);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);

        if(branch == null)
        {
            branch = new RecursiveTree<>();
            branches.put(path[index], branch);
        }

        return branch.setAt_internal(newItem, path, index + 1);
    }

    private ValueWithPresence<TLeaf> setAt_internal(TLeaf newItem, List<TNode> path, int index)
    {
        if(path.size() == index)
            return setRootItem(newItem);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);

        if(branch == null)
        {
            branch = new RecursiveTree<>();
            branches.put(path.get(index), branch);
        }

        return branch.setAt_internal(newItem, path, index + 1);
    }

    @Override
    public ValueWithPresence<TLeaf> setAt(TLeaf newItem, TNode id)
    {
        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(id, null);

        if(branch == null)
        {
            branch = new RecursiveTree<>();
            branches.put(id, branch);
        }

        return branch.setRootItem(newItem);
    }

    @Override
    public ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, TNode... path)
    { return setAtIfAbsent_internal(newItem, path, 0); }

    @Override
    public ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, List<TNode> path)
    { return setAtIfAbsent_internal(newItem, path, 0); }

    private ValueWithPresence<TLeaf> setAtIfAbsent_internal(TLeaf newItem, TNode[] path, int index)
    {
        if(path.length == index)
            return setRootItemIfAbsent(newItem);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);

        if(branch == null)
        {
            branch = new RecursiveTree<>();
            branches.put(path[index], branch);
        }

        return branch.setAtIfAbsent_internal(newItem, path, index + 1);
    }

    private ValueWithPresence<TLeaf> setAtIfAbsent_internal(TLeaf newItem, List<TNode> path, int index)
    {
        if(path.size() == index)
            return setRootItemIfAbsent(newItem);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);

        if(branch == null)
        {
            branch = new RecursiveTree<>();
            branches.put(path.get(index), branch);
        }

        return branch.setAtIfAbsent_internal(newItem, path, index + 1);
    }

    @Override
    public ValueWithPresence<TLeaf> setAtIfAbsent(TLeaf newItem, TNode id)
    {
        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(id, null);

        if(branch == null)
        {
            branch = new RecursiveTree<>();
            branches.put(id, branch);
        }

        return branch.setRootItemIfAbsent(newItem);
    }

    @Override
    public void clear()
    {
        hasRootItem = false;
        rootItem = null;
        branches.clear();
    }

    @Override
    public void clearNonRootItems()
    { branches.clear(); }

    @Override
    public ValueWithPresence<TLeaf> clearRootItem()
    {
        ValueWithPresence<TLeaf> r = new ValueWithPresence<>(hasRootItem, rootItem);
        hasRootItem = false;
        rootItem = null;
        return r;
    }

    @Override
    public ValueWithPresence<TLeaf> clearRootItemIf(Predicate<TLeaf> test)
    {
        ValueWithPresence<TLeaf> r = new ValueWithPresence<>(hasRootItem, rootItem);

        if(hasRootItem && test.test(rootItem))
        {
            hasRootItem = false;
            rootItem = null;
        }

        return r;
    }

    @Override
    public ValueWithPresence<TLeaf> clearAt(TNode... path)
    { return clearAt_internal(path, 0); }

    @Override
    public ValueWithPresence<TLeaf> clearAt(List<TNode> path)
    { return clearAt_internal(path, 0); }

    private ValueWithPresence<TLeaf> clearAt_internal(TNode[] path, int index)
    {
        if(path.length == index)
            return clearRootItem();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? new ValueWithPresence<>(false, null) : branch.clearAt_internal(path, index + 1);
    }

    private ValueWithPresence<TLeaf> clearAt_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
            return clearRootItem();

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? new ValueWithPresence<>(false, null) : branch.clearAt_internal(path, index + 1);
    }

    @Override
    public ValueWithPresence<TLeaf> clearAtIf(TNode[] path, Predicate<TLeaf> test)
    { return clearAtIf_internal(path, test, 0); }

    @Override
    public ValueWithPresence<TLeaf> clearAtIf(List<TNode> path, Predicate<TLeaf> test)
    { return clearAtIf_internal(path, test, 0); }

    private ValueWithPresence<TLeaf> clearAtIf_internal(TNode[] path, Predicate<TLeaf> test, int index)
    {
        if(path.length == index)
            return clearRootItemIf(test);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);
        return branch == null ? new ValueWithPresence<>(false, null) : branch.clearAtIf_internal(path, test, index + 1);
    }

    private ValueWithPresence<TLeaf> clearAtIf_internal(List<TNode> path, Predicate<TLeaf> test, int index)
    {
        if(path.size() == index)
            return clearRootItemIf(test);

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);
        return branch == null ? new ValueWithPresence<>(false, null) : branch.clearAtIf_internal(path, test, index + 1);
    }

    @Override
    public void clearAtAndUnder(TNode... path)
    { clearAtAndUnder_internal(path, 0); }

    @Override
    public void clearAtAndUnder(List<TNode> path)
    { clearAtAndUnder_internal(path, 0); }

    private void clearAtAndUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
        {
            clear();
            return;
        }

        if(path.length == (index + 1))
        {
            branches.remove(path[index]);
            return;
        }

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);

        if(branch != null)
            branch.clearAtAndUnder_internal(path, index + 1);
    }

    private void clearAtAndUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
        {
            clear();
            return;
        }

        if(path.size() == (index + 1))
        {
            branches.remove(path.get(index));
            return;
        }

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);

        if(branch != null)
            branch.clearAtAndUnder_internal(path, index + 1);
    }

    @Override
    public void clearAtAndUnder(TNode id)
    { branches.remove(id); }

    @Override
    public void clearUnder(TNode... path)
    { clearUnder_internal(path, 0); }

    @Override
    public void clearUnder(List<TNode> path)
    { clearUnder_internal(path, 0); }

    public void clearUnder_internal(TNode[] path, int index)
    {
        if(path.length == index)
        {
            clearNonRootItems();
            return;
        }

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path[index], null);

        if(branch == null)
            return;

        if((path.length == (index + 1)) && (!branch.hasRootItem))
        {
            branches.remove(path[index]);
            return;
        }

        branch.clearUnder_internal(path, index + 1);
    }

    public void clearUnder_internal(List<TNode> path, int index)
    {
        if(path.size() == index)
        {
            clearNonRootItems();
            return;
        }

        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(path.get(index), null);

        if(branch == null)
            return;

        if((path.size() == (index + 1)) && (!branch.hasRootItem))
        {
            branches.remove(path.get(index));
            return;
        }

        branch.clearUnder_internal(path, index + 1);
    }

    @Override
    public void clearUnder(TNode id)
    {
        RecursiveTree<TNode, TLeaf> branch = branches.getOrDefault(id, null);

        if(branch == null)
            return;

        if(!branch.hasRootItem)
            branches.remove(id);

        branch.clearNonRootItems();
    }

    @Override
    public List<TLeaf> toList()
    { return getItemsAsStream().collect(Collectors.toList()); }

    @Override
    public List<TLeaf> toList(Comparator<TNode> comparator)
    { return getItemsInOrderAsStream(comparator).collect(Collectors.toList()); }

    @Override
    public RecursiveTree<TNode, TLeaf> copy()
    {
        RecursiveTree<TNode, TLeaf> result = new RecursiveTree<>();
        result.hasRootItem = this.hasRootItem;
        result.rootItem = this.rootItem;

        for(Map.Entry<TNode, RecursiveTree<TNode, TLeaf>> branchEntry : branches.entrySet())
            result.branches.put(branchEntry.getKey(), branchEntry.getValue().copy());

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;

        if(o == null || getClass() != o.getClass())
            return false;

        RecursiveTree<?, ?> ot = (RecursiveTree<?, ?>) o;

        return (hasRootItem ? (ot.hasRootItem || Objects.equals(rootItem, ot.rootItem)) : !ot.hasRootItem)
            && (branches.equals(ot.branches));
    }

    @Override
    public int hashCode()
    { return Objects.hash(rootItem, hasRootItem, branches); }
}