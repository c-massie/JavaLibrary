package scot.massie.lib.collections.trees;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreePath<TNode>
{
    protected List<TNode> nodes;

    //region initialisation
    public TreePath()
    { this.nodes = Collections.emptyList(); }

    public TreePath(List<? extends TNode> path)
    { this.nodes = Collections.unmodifiableList(path); }

    @SafeVarargs
    public TreePath(TNode... path)
    { this.nodes = Collections.unmodifiableList(Arrays.asList(path)); }

    public static <TNode> TreePath<TNode> fromStream(Stream<? extends TNode> path)
    { return new TreePath<>(path.collect(Collectors.toList())); }

    public static <TNode> TreePath<TNode> root()
    { return new TreePath<>(); }
    //endregion

    //region methods
    public static <TNode extends Comparable<TNode>> Comparator<TreePath<TNode>> getComparator()
    { return getComparator(Comparator.naturalOrder()); }

    public static <TNode> Comparator<TreePath<TNode>> getComparator(Comparator<? super TNode> nodeComparator)
    {
        // NOTE: Regardless of how the provided comparator treats nulls, the resultant comparator treats null nodes as
        //       coming before/being "less than" all other nodes.

        return (a, b) ->
        {
            if(a == null)
                return b == null ? 0 : -1;

            if(b == null)
                return 1;

            final int aSize = a.nodes.size();
            final int bSize = b.nodes.size();

            if(aSize == 0)
                return bSize == 0 ? 0 : -1;

            for(int i = 0; i < aSize; i++)
            {
                TNode aNode = a.nodes.get(i);

                if(i >= bSize)
                    return 1;

                TNode bNode = b.nodes.get(i);

                if(aNode == null && bNode == null)
                    continue;

                if(aNode == null)
                    return -1;

                if(bNode == null)
                    return 1;

                int comparison = nodeComparator.compare(aNode, bNode);

                if(comparison != 0)
                    return comparison;
            }

            return bSize > aSize ? -1 : 0;
        };
    }

    public List<TNode> getNodes()
    { return nodes; }

    public TNode getNode(int index)
    { return nodes.get(index); }

    public int size()
    { return nodes.size(); }

    public TNode first()
    {
        if(nodes.isEmpty())
            throw new NoSuchElementException();

        return nodes.get(0);
    }

    public TNode firstOrNull()
    { return nodes.isEmpty() ? null : nodes.get(0); }

    public TNode last()
    {
        if(nodes.isEmpty())
            throw new NoSuchElementException();

        return nodes.get(nodes.size() - 1);
    }

    public TNode lastOrNull()
    { return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1); }

    public TreePath<TNode> getParent()
    { return (nodes.size() >= 1) ? (new TreePath<>(nodes.subList(0, nodes.size() - 1))) : (null); }

    public TreePath<TNode> truncateTo(int newLength)
    {
        if(newLength < 0)
            throw new IllegalArgumentException("TreePath.truncateTo can only shorten tree paths to a positive number "
                                               + "of nodes");

        if(newLength >= nodes.size())
            return this;

        return new TreePath<>(nodes.subList(0, newLength));
    }

    public TreePath<TNode> withoutFirstNodes(int numberOfNodesToDrop)
    {
        if(numberOfNodesToDrop < 0)
            throw new IllegalArgumentException("TreePath.withoutFirstNodes can only drop a positive number of initial "
                                               + "nodes.");

        if(numberOfNodesToDrop == 0)
            return this;

        return new TreePath<>(nodes.subList(numberOfNodesToDrop, nodes.size()));
    }

    public TreePath<TNode> withoutLastNodes(int numberOfNodesToDrop)
    {
        if(numberOfNodesToDrop < 0)
            throw new IllegalArgumentException("TreePath.withoutLastNodes can only drop a positive number of final "
                                               + "nodes.");

        if(numberOfNodesToDrop == 0)
            return this;

        return new TreePath<>(nodes.subList(0, nodes.size() - numberOfNodesToDrop));
    }

    public TreePath<TNode> appendedWith(TreePath<? extends TNode> other)
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        resultNodes.addAll(other.nodes);
        return new TreePath<>(resultNodes);
    }

    public TreePath<TNode> appendedWith(TNode node)
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        resultNodes.add(node);
        return new TreePath<>(resultNodes);
    }

    public TreePath<TNode> prependedWith(TreePath<? extends TNode> other)
    {
        List<TNode> resultNodes = new ArrayList<>(other.nodes);
        resultNodes.addAll(nodes);
        return new TreePath<>(resultNodes);
    }

    public TreePath<TNode> prependedWith(TNode node)
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        resultNodes.add(0, node);
        return new TreePath<>(resultNodes);
    }

    public TreePath<TNode> reversed()
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        Collections.reverse(resultNodes);
        return new TreePath<>(resultNodes);
    }

    public boolean isAncestorOf(TreePath<TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Other was null.");

        if(nodes.size() >= other.nodes.size())
            return false;

        return nodes.equals(other.nodes.subList(0, nodes.size()));
    }

    public boolean isEqualToOrAncestorOf(TreePath<TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Other was null.");

        if(nodes.size() > other.nodes.size())
            return false;

        if(nodes.size() == other.nodes.size())
            return nodes.equals(other.nodes);

        return nodes.equals(other.nodes.subList(0, nodes.size()));
    }

    public boolean isDescendantOf(TreePath<TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Other was null.");

        if(nodes.size() <= other.nodes.size())
            return false;

        return nodes.subList(0, other.nodes.size()).equals(other.nodes);
    }

    public boolean isEqualToOrDescendantOf(TreePath<TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Other was null.");

        if(nodes.size() < other.nodes.size())
            return false;

        if(nodes.size() == other.nodes.size())
            return nodes.equals(other.nodes);

        return nodes.subList(0, other.nodes.size()).equals(other.nodes);
    }

    public boolean isRoot()
    { return nodes.isEmpty(); }

    public boolean equals(TreePath<TNode> other)
    { return (other != null) && (nodes.equals(other.nodes)); }

    @Override
    public boolean equals(Object other)
    { return (other instanceof TreePath) && (nodes.equals(((TreePath<?>)other).nodes)); }

    @Override
    public int hashCode()
    { return Objects.hash(nodes); }

    @Override
    public String toString()
    {
        if(nodes.isEmpty())
            return "(root)";

        return nodes.stream().map(x -> x == null ? "(null)" : x.toString()).collect(Collectors.joining("."));
    }

    public static boolean areAllComparable(Iterable<? extends TreePath<?>> paths)
    {
        Iterator<? extends TreePath<?>> pathIter = paths.iterator();

        if(!pathIter.hasNext())
            return false;

        TreePath<?> firstPath = pathIter.next();
        Type expectedComparableType = firstPath.getComparableType();

        if(expectedComparableType == null)
            return false;

        while(pathIter.hasNext())
            if(pathIter.next().getComparableType() != expectedComparableType)
                return false;

        return true;
    }

    public Type getComparableType()
    {
        // This is needed to establish whether or not all nodes in the path are comparable with eachother in the absence
        // of reified generics.

        if(nodes.isEmpty())
            return null;

        Type expected = getComparableTypeFromNode(nodes.get(0));

        for(int i = 1; i < nodes.size(); i++)
        {
            TNode iNode = nodes.get(i);

            if(getComparableTypeFromNode(iNode) != expected)
                return null;
        }

        return expected;
    }

    private static <TNode> Type getComparableTypeFromNode(TNode node)
    { return getComparableTypeFromClass(node.getClass()); }

    private static Type getComparableTypeFromClass(@SuppressWarnings("rawtypes")
                                                           /* Don't know the specific class, don't need to. */
                                                           Class t)
    {
        Type result = getComparableTypeFromInterface(t);

        if(result != null)
            return result;

        @SuppressWarnings("rawtypes") /* Don't know the specific class, don't need to. */
        Class parentClass = t.getSuperclass();

        if(parentClass != null)
            result = getComparableTypeFromClass(parentClass);

        return result;
    }

    private static Type getComparableTypeFromInterface(@SuppressWarnings("rawtypes")
                                                               /* Don't know the specific class, don't need to. */
                                                               Class t)
    {
        for(Type ti : t.getGenericInterfaces())
            if(ti.getTypeName().startsWith("java.lang.Comparable<"))
                return ((ParameterizedType)ti).getActualTypeArguments()[0];

        for(@SuppressWarnings("rawtypes") /* Don't know the specific class, don't need to. */ Class tiface
                : t.getInterfaces())
        {
            Type result = getComparableTypeFromInterface(tiface);

            if(result != null)
                return result;
        }

        return null;
    }

    //endregion
}
