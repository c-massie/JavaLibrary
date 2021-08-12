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

/**
 * <p>An ordered collection of nodes for traversing {@link Tree Trees}.</p>
 *
 * <p>Tree paths are immutable.</p>
 *
 * <p>In documentation, note that:</p>
 *
 * <ul>
 *     <li>
 *         "Root" refers to an empty tree path of no nodes.
 *     </li>
 *     <li>
 *         One path is said to be "under", "below", or a "descendant" of another path where it's longer than and begins
 *         with the other path.
 *     </li>
 *     <li>
 *         One path is said to be "at" another path where they're equal.
 *     </li>
 *     <li>
 *         One path is said to be "above", "along", or an "ancestor" of another path where the other path is "under" it.
 *     </li>
 *     <li>
 *         One path is said to be a "parent" of another path where it is one node longer than the other and the other
 *         begins with it.
 *     </li>
 *     <li>
 *         One path is said to be a "child" of another path where the other is its "parent".
 *     </li>
 * </ul>
 * @param <TNode> The type of the nodes making up the tree path.
 */
public final class TreePath<TNode>
{
    @SuppressWarnings("rawtypes") // This is parameterised using the .root() static method.
    private static final TreePath ROOT = new TreePath();

    /**
     * The nodes of this tree path.
     */
    private final List<TNode> nodes;

    //region initialisation

    /**
     * Creates a new tree path instance, representing root.
     */
    public TreePath()
    { this.nodes = Collections.emptyList(); }

    /**
     * Creates a new tree path instance, made up of the nodes in the provided list in order.
     * @param path The list of nodes to turn into a tree path.
     */
    public TreePath(List<? extends TNode> path)
    { this.nodes = Collections.unmodifiableList(path); }

    /**
     * Creates a new tree path instance, made up of the provided nodes in order.
     * @param path The nodes to turn into a tree path.
     */
    @SafeVarargs
    public TreePath(TNode... path)
    { this.nodes = Collections.unmodifiableList(Arrays.asList(path)); }

    /**
     * Creates a new tree path instance, made up of the nodes provided by a stream in the order provided by the stream.
     * @param path The stream providing nodes to turn into a tree path.
     * @param <TNode> The type of the tree path to create.
     * @return A new tree path instance.
     */
    public static <TNode> TreePath<TNode> fromStream(Stream<? extends TNode> path)
    { return new TreePath<>(path.collect(Collectors.toList())); }

    /**
     * Gets an empty "root" tree path.
     * @param <TNode> The type of the tree path.
     * @return A "root" tree path.
     */
    public static <TNode> TreePath<TNode> root()
    {
        // This method parameterises the root instance, which doesn't contain any nodes of any type.
        //noinspection unchecked
        return ROOT;
    }
    //endregion

    //region methods

    /**
     * Gets a comparator for comparing tree paths of a given comparable type. Tree nodes are ordered such that paths of
     * equal length are compared by their nodes from start to finish, (until a difference is found) and where a path is
     * immediately followed by its own descendants before a following equal length or its descendants. Root always comes
     * first.
     * @param <TNode> The type of the tree paths to be compared.
     * @return A new comparator comparing tree paths using the method described in natural order.
     */
    public static <TNode extends Comparable<TNode>> Comparator<TreePath<TNode>> getComparator()
    { return getComparator(Comparator.naturalOrder()); }

    /**
     * Gets a comparator for comparing tree paths of a given type using a comparator to compare individual nodes. Tree
     * nodes are ordered such that paths of equal length are compared by their nodes from start to finish, (until a
     * difference is found) and where a path is immediately followed by its own descendants before a following equal
     * length or its descendants. Root always comes first.
     * @param nodeComparator The comparator for comparing individual nodes.
     * @param <TNode> The type of the tree paths to be compared.
     * @return A new comparator comparing tree paths using the method described with the provided comparator.
     */
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

    /**
     * Gets the nodes representing this tree path.
     * @return The nodes representing this tree path, in order from ancestor to descendant.
     */
    public List<TNode> getNodes()
    { return nodes; }

    /**
     * Gets the node at the given index of this tree path when considered as a list.
     * @param index The index of the node in the tree path to get.
     * @return The node in the tree path at the given index.
     * @throws IndexOutOfBoundsException If the given index is outwith the bounds of the tree path.
     */
    public TNode getNode(int index)
    {
        if(index < 0)
            throw new IndexOutOfBoundsException("Cannot get nodes at negative indices.");

        if(index >= nodes.size())
            throw new IndexOutOfBoundsException("Cannot get nodes at indices greater than or equal to the path's size.");

        return nodes.get(index);
    }

    /**
     * Gets the number of nodes in this tree path.
     * @return The number of nodes in this tree path.
     */
    public int size()
    { return nodes.size(); }

    /**
     * Gets the first node of this tree path.
     * @return The first node of this tree path.
     * @throws NoSuchElementException If this tree path is root. (and thus has no nodes)
     */
    public TNode first()
    {
        if(nodes.isEmpty())
            throw new NoSuchElementException();

        return nodes.get(0);
    }

    /**
     * Gets the first node of this tree path, or null if this tree path is root.
     * @return The first node of this tree path, or null if this tree path is root.
     */
    public TNode firstOrNull()
    { return nodes.isEmpty() ? null : nodes.get(0); }

    /**
     * Gets the last node of this tree path.
     * @return The last node of this tree path.
     * @throws NoSuchElementException If this tree path is root. (and thus has no nodes)
     */
    public TNode last()
    {
        if(nodes.isEmpty())
            throw new NoSuchElementException();

        return nodes.get(nodes.size() - 1);
    }

    /**
     * Gets the last node of this tree path, or null if this tree path is root.
     * @return The last node of this tree path, or null if this tree path is root.
     */
    public TNode lastOrNull()
    { return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1); }

    /**
     * Gets this tree path's parent path.
     * @return This path truncated by 1, or null if this path is root. (and thus has no parent)
     */
    public TreePath<TNode> getParent()
    { return (nodes.size() >= 1) ? (new TreePath<>(nodes.subList(0, nodes.size() - 1))) : (null); }

    /**
     * Gets this tree path truncated to the given size.
     * @param newLength The maximum size of the tree path to produce.
     * @return This path truncated to the given length. If the given length is longer than or equal to this path, just
     *         returns this path.
     * @throws IllegalArgumentException If the given new length is negative.
     */
    public TreePath<TNode> truncateTo(int newLength)
    {
        if(newLength < 0)
            throw new IllegalArgumentException("TreePath.truncateTo can only shorten tree paths to a positive number "
                                               + "of nodes");

        if(newLength >= nodes.size())
            return this;

        if(newLength == 0)
            return root();

        return new TreePath<>(nodes.subList(0, newLength));
    }

    /**
     * Gets this tree path with the given number of nodes removed from the start.
     * @param numberOfNodesToDrop The number of nodes to skip from the start.
     * @return This path, shortened by the given number of nodes, missing them from the start.
     * @throws IllegalArgumentException If the given number of nodes to drop is negative, or greater than the number of
     *                                  nodes in this path.
     */
    public TreePath<TNode> withoutFirstNodes(int numberOfNodesToDrop)
    {
        if(numberOfNodesToDrop < 0)
            throw new IllegalArgumentException("TreePath.withoutFirstNodes can only drop a positive number of initial "
                                               + "nodes.");

        if(numberOfNodesToDrop > nodes.size())
            throw new IllegalArgumentException("This tree path does not " + numberOfNodesToDrop + " that many nodes to "
                                               + "drop, it only has " + size() + " nodes.");

        if(numberOfNodesToDrop == 0)
            return this;

        if(numberOfNodesToDrop == nodes.size())
            return root();

        return new TreePath<>(nodes.subList(numberOfNodesToDrop, nodes.size()));
    }

    /**
     * Gets this tree path truncated by the number of nodes.
     * @param numberOfNodesToDrop The number of nodes to truncate from the end.
     * @return This path, shortened by the given number of nodes, missing them from the end.
     */
    public TreePath<TNode> withoutLastNodes(int numberOfNodesToDrop)
    {
        if(numberOfNodesToDrop < 0)
            throw new IllegalArgumentException("TreePath.withoutLastNodes can only drop a positive number of final "
                                               + "nodes.");

        if(numberOfNodesToDrop > nodes.size())
            throw new IllegalArgumentException("This tree path does not " + numberOfNodesToDrop + " that many nodes to "
                                               + "drop, it only has " + size() + " nodes.");

        if(numberOfNodesToDrop == 0)
            return this;

        if(numberOfNodesToDrop == nodes.size())
            return root();

        return new TreePath<>(nodes.subList(0, nodes.size() - numberOfNodesToDrop));
    }

    /**
     * Gets this path with the nodes of the given path added to the end.
     * @param other The tree path providing nodes to add to the end.
     * @return A new tree path with the nodes of the given path added to the end.
     */
    public TreePath<TNode> appendedWith(TreePath<? extends TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Argument was null.");

        List<TNode> resultNodes = new ArrayList<>(nodes);
        resultNodes.addAll(other.nodes);
        return new TreePath<>(resultNodes);
    }

    /**
     * Gets this path with the given node added to the end.
     * @param node The node to add to the end.
     * @return A new tree path with the given node added to the end.
     */
    public TreePath<TNode> appendedWith(TNode node)
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        resultNodes.add(node);
        return new TreePath<>(resultNodes);
    }

    /**
     * Gets this path with the nodes of the given path added to the start.
     * @param other The tree path providing nodes to add to the start.
     * @return A new tree path with the nodes of the given path added to the start.
     */
    public TreePath<TNode> prependedWith(TreePath<? extends TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Argument was null.");

        List<TNode> resultNodes = new ArrayList<>(other.nodes);
        resultNodes.addAll(nodes);
        return new TreePath<>(resultNodes);
    }

    /**
     * Gets this path with the given node added to the start.
     * @param node The node to add to the start.
     * @return A new tree path with the given node added to the start.
     */
    public TreePath<TNode> prependedWith(TNode node)
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        resultNodes.add(0, node);
        return new TreePath<>(resultNodes);
    }

    /**
     * Gets this path with its nodes in reverse order.
     * @return A new tree path with this path's nodes in reverse order.
     */
    public TreePath<TNode> reversed()
    {
        List<TNode> resultNodes = new ArrayList<>(nodes);
        Collections.reverse(resultNodes);
        return new TreePath<>(resultNodes);
    }

    /**
     * Gets whether the given tree path begins with the nodes of this one.
     * @param other The other tree path to check.
     * @return True if the given tree path is longer than this one and begins with this tree path's nodes in order.
     *         Otherwise, false
     */
    public boolean isAncestorOf(TreePath<TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Other was null.");

        if(nodes.size() >= other.nodes.size())
            return false;

        return nodes.equals(other.nodes.subList(0, nodes.size()));
    }

    /**
     * Gets whether the given tree path is equal to or begins with the nodes of this one.
     * @param other The other tree path to check.
     * @return True if the given tree path is equal to this one, or is longer than this one and begins with this tree
     *         path's nodes in order. Otherwise, false.
     */
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

    /**
     * Gets whether this tree path begins with the nodes of the given one.
     * @param other The other tree path to check.
     * @return True if this tree path is longer than the given one and begins with its nodes in order. Otherwise, false.
     */
    public boolean isDescendantOf(TreePath<TNode> other)
    {
        if(other == null)
            throw new IllegalArgumentException("Other was null.");

        if(nodes.size() <= other.nodes.size())
            return false;

        return nodes.subList(0, other.nodes.size()).equals(other.nodes);
    }

    /**
     * Gets whether this tree path is equal or begins with the nodes of the given one.
     * @param other The other tree path to check.
     * @return True if this tree path is equal to the given one, or is longer than it and begins with its nodes in
     *         order. Otherwise, false.
     */
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

    /**
     * Gets whether this tree path represents root.
     * @return True if this tree path contains no nodes and thus represents root. Otherwise, false.
     */
    public boolean isRoot()
    { return nodes.isEmpty(); }

    /**
     * Gets whether this tree path is equal to the other one. Two tree paths are considered equal where they contain the
     * same nodes in the same order.
     * @param other The other tree path to check.
     * @return True if both this tree path and the given one contain the same nodes in the same order. Otherwise, false.
     */
    public boolean equals(TreePath<TNode> other)
    { return (other != null) && (nodes.equals(other.nodes)); }

    /**
     * Gets whether this tree path is equal to the other one. Two tree paths are considered equal where they contain the
     * same nodes in the same order.
     * @param other The other tree path to check.
     * @return True if both this tree path and the given one contain the same nodes in the same order. Otherwise, false.
     */
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

    /**
     * Gets whether all tree paths in the given iterable are comparable with each other.
     * @param paths The tree paths to check.
     * @return True if all tree paths in the given iterable object are comparable with each other. Otherwise, false.
     */
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

    /**
     * Gets the type this tree path's nodes are comparable with.
     * @return The type this tree path's nodes are comparable with via the Comparable interface, or null if they're not
     *         all comparable and comparable with each other.
     */
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

    /**
     * Gets the type the given node is comparable with.
     * @param node The node to get the type it's comparable with.
     * @param <TNode> The type of the node.
     * @return The type the given node uses to implement the Comparable interface, or null if it does not.
     */
    private static <TNode> Type getComparableTypeFromNode(TNode node)
    { return getComparableTypeFromClass(node.getClass()); }

    /**
     * Gets the type a given class uses to implement Comparable, or null if it does not.
     * @param t The class to get the comparable type of.
     * @return The type the given class uses to implement Comparable, or null if it does not.
     */
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

    /**
     * Gets the type a given interface uses to implement Comparable, or null if it does not.
     * @param t The interface to get the comparable type of.
     * @return The type the given interface uses to implement Comparable, or null if it does not.
     */
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
