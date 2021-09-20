package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static scot.massie.lib.collections.trees.TreeUtils.*;

/**
 * <p>Tree data structure that stores items by placing items at paths and accessing them based on those paths.</p>
 *
 * <p>Paths are represented by the {@link TreePath} type, an ordered collection of nodes for navigating trees. See
 * associated terminology.</p>
 *
 * @implNote If an implementation of a tree doesn't allow null values, assigning null to a path in a tree should be
 *           treated as clearing the item from that path.
 * @param <TNode> The type of the nodes of the paths used.
 * @param <TLeaf> The type of the items being stored.
 */
public interface Tree<TNode, TLeaf> extends Iterable<TreeEntry<TNode, TLeaf>>
{
    //region check state of contents
    //region has items

    /**
     * Gets whether this tree contains any items at any paths.
     * @return True if this tree contains any items at any paths. Otherwise, false.
     */
    boolean hasItems();

    /**
     * Gets whether this tree contains any items at any paths other than root.
     * @return True if this tree contains any items at any paths other than root. Otherwise, false.
     */
    boolean hasNonRootItems();

    /**
     * Gets whether this tree contains any items at any paths starting with (including) the given path.
     * @param path The path to check for the presence of any items at or under.
     * @return True if this tree contains any items at any paths starting with (including) the given path.
     */
    default boolean hasItemsAtOrUnder(TreePath<TNode> path)
    { return getBranchView(path).hasItems(); }

    /**
     * Gets whether this tree contains any items at any paths starting with (but not including) the given path.
     * @param path The path to check for the presence of any items under.
     * @return True if this tree contains any items at any paths starting with (but not including) the given path.
     */
    default boolean hasItemsUnder(TreePath<TNode> path)
    { return getBranchView(path).hasNonRootItems(); }

    /**
     * Gets whether this tree contains any items at any paths the given paths start with, including root.
     * @param path The path to check for the presence for any items along.
     * @return True if this tree contains any items at any paths the given path starts with, including root. Otherwise,
     *         false.
     */
    boolean hasItemsAlong(TreePath<TNode> path);

    /**
     * Gets whether this tree contains any items at any paths the given paths start with, not including root.
     * @param path The path to check for the presence for any items along.
     * @return True if this tree contains any items at any paths the given path starts with, not including root.
     *         Otherwise, false.
     */
    boolean hasNonRootItemsAlong(TreePath<TNode> path);

    /**
     * Gets whether this tree contains an item at the given path.
     * @param path The path to check for the presence of an item at.
     * @return True if this tree contains an item at the given path. Otherwise, false.
     */
    boolean hasItemAt(TreePath<TNode> path);

    /**
     * Gets whether this tree contains an item at root.
     * @return True if this tree contains an item at root. Otherwise, false.
     */
    boolean hasRootItem();
    //endregion

    //region is empty

    /**
     * Gets whether this tree contains no items.
     * @return True if this tree contains no items. Otherwise, false.
     */
    default boolean isEmpty()
    { return !hasItems(); }

    /**
     * Gets whether this tree contains no items at any paths other than root.
     * @return True if this tree contains no items at any paths other than root. Otherwise, false.
     */
    default boolean isEmptyUnderRoot()
    { return !hasNonRootItems(); }

    /**
     * Gets whether this tree contains no items at any paths beginning with (including) the given path.
     * @param path The path to check for the presence of any items at or under.
     * @return True if this tree contains any items at paths starting with (including) the given path. Otherwise, false.
     */
    default boolean isEmptyAtAndUnder(TreePath<TNode> path)
    { return !hasItemsAtOrUnder(path); }

    /**
     * Gets whether this tree contains no items at any paths beginning with (but not including) the given path.
     * @param path The path to check for the presence of any items under.
     * @return True if this tree contains any items at paths starting with (but not including) the given path.
     *         Otherwise, false.
     */
    default boolean isEmptyUnder(TreePath<TNode> path)
    { return !hasItemsUnder(path); }

    /**
     * Gets whether this tree contains no items at any paths the given path starts with. (Including root)
     * @param path The path to check for the presence of any items along.
     * @return True if this tree contains any items at any paths the given path starts with. (Including root) Otherwise,
     *         false
     */
    default boolean isEmptyAlong(TreePath<TNode> path)
    { return !hasItemsAlong(path); }

    /**
     * Gets whether this tree contains no items at any paths the given path starts with. (But not including root)
     * @param path The path to check for the presence of any items along.
     * @return True if this tree contains any items at any paths the given path starts with. (But not including root)
     *         Otherwise, false.
     */
    default boolean isEmptyAlongAfterRoot(TreePath<TNode> path)
    { return !hasNonRootItemsAlong(path); }

    /**
     * Gets whether this tree contains no items at the given path.
     * @param path The path to check for the presence of an item at.
     * @return True if this tree contains an item at the given path. Otherwise, false.
     */
    default boolean isEmptyAt(TreePath<TNode> path)
    { return !hasItemAt(path); }

    /**
     * Gets whether this tree contains no item at root.
     * @return True if this tree contains no item at root. Otherwise, false.
     */
    default boolean isEmptyAtRoot()
    { return !hasRootItem(); }
    //endregion
    //endregion

    //region get stored items
    //region root

    /**
     * Gets the item in this tree at root.
     * @return The item in this tree at root.
     * @throws NoItemAtPathException If no item exists in this tree at root.
     */
    TLeaf getRootItem() throws NoItemAtPathException;

    /**
     * Gets the item in this tree at root, or a given default item if no item exists at root.
     * @param defaultItem The item to return if this tree contains no item at root.
     * @return The item in this tree at root, or the given item if no item exists in this tree at root.
     */
    TLeaf getRootItemOr(TLeaf defaultItem);

    /**
     * Gets the item in this tree at root, or a given default item if no item exists at root.
     * @param defaultItem The item to return if this tree contains no item at root.
     * @return The item in this tree at root, or the given item if no item exists in this tree at root.
     */
    Object getRootItemOrDefaultOfAnyType(Object defaultItem);

    /**
     * Gets the item in this tree at root, or null if no item exists at root.
     * @return The item in this tree at root, or null if no item exists in this tree at root.
     */
    default TLeaf getRootItemOrNull()
    { return getRootItemOr(null); }
    //endregion

    //region leaves

    /**
     * Gets the item in this tree at the given path.
     * @param path The path of the item to get.
     * @return The item in this tree at the given path.
     * @throws NoItemAtPathException If no item exists in this tree at the given path.
     */
    TLeaf getAt(TreePath<TNode> path) throws NoItemAtPathException;

    /**
     * Gets the item in this tree at the given path, or a given default item if no item exists at the given path.
     * @param path The path of the item to get.
     * @param defaultItem The item to return if this tree contains no item at the given path.
     * @return The item in this tree at the given path, or the given item if no item exists in this tree at the given
     *         path.
     */
    TLeaf getAtOr(TreePath<TNode> path, TLeaf defaultItem);

    /**
     * Gets the item in this tree at the given path, or a given default item if no item exists at the given path.
     * @param path The path of the item to get.
     * @param defaultItem The item to return if this tree contains no item at the given path.
     * @return The item in this tree at the given path, or the given item if no item exists in this tree at the given
     *         path.
     */
    Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem);

    /**
     * Gets the item in this tree at the given path, or null if no item exists at the given path.
     * @param path The path of the item to get.
     * @return The item in this tree at the given path, or null if no item exists in this tree at the given path.
     */
    default TLeaf getAtOrNull(TreePath<TNode> path)
    { return getAtOr(path, null); }
    //endregion
    //endregion

    //region get multiple stored items

    /**
     * Gets all items in this tree at any path.
     * @return A collection of all items in this tree at any path.
     */
    Collection<TLeaf> getItems();

    /**
     * Gets all items in this tree at any path, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of all items in this tree at any path, ordered by tree paths using the given comparator to compare
     *         their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets all items in this tree that match the given predicate.
     * @param test The predicate that items and their paths must adhere to to be included in the returned collection.
     * @return A collection of all the items in this tree that adhere to the given predicate.
     */
    Collection<TLeaf> getItemsWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    /**
     * Gets all items in this tree whose paths match the given predicate.
     * @param test The predicate that items' paths must adhere to to be included in the returned collection.
     * @return A collection of all items in this tree whose paths adhere to the given predicate.
     */
    Collection<TLeaf> getItemsWherePath(Predicate<? super TreePath<TNode>> test);

    /**
     * Gets all items in this tree that match the given predicate, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @param test The predicate that items and their paths must adhere to to be included in the returned collection.
     * @return A list of all items in this tree that adhere to the given predicate, ordered by tree paths using the
     *         given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator,
                                           BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    /**
     * Gets all items in this tree whose paths match the given predicate, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @param test The predicate that items' paths must adhere to to be included in the returned collection.
     * @return A list of all items in this tree whose paths adhere to the given predicate, ordered by tree paths using
     *         the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator,
                                               Predicate<? super TreePath<TNode>> test);

    /**
     * Gets all items in this tree other than root.
     * @return A collection of all the items in this tree other than the item at root, if any.
     */
    Collection<TLeaf> getItemsUnderRoot();

    /**
     * Gets all items in this tree other than root, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of all the items in this tree, ordered by the tree paths using the given comparator to compare
     *         the nodes.
     * @see TreePath#getComparator(Comparator)
     */
    List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets all items in this tree whose paths start with (including) the given path.
     * @param path The path to use to get items in this tree at and under.
     * @return A collection of all the items in this tree whose paths start with (including) the given path.
     */
    default Collection<TLeaf> getItemsAtAndUnder(TreePath<TNode> path)
    { return getBranchView(path).getItems(); }

    /**
     *  Gets all items in this tree whose paths start with (including) the given path, in order.
     * @param path The path to use to get items in this tree at and under.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of all the items in this tree whose paths start with (including) the given path, ordered by tree
     *         paths using the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TLeaf> getItemsAtAndUnderInOrder(TreePath<TNode> path, Comparator<? super TNode> comparator)
    { return getBranchView(path).getItemsInOrder(comparator); }

    /**
     * Gets all items in this tree whose paths start with (but not including) the given path.
     * @param path The path to use to get items in this tree under.
     * @return A collection of all the items in this tree whose paths start with (but not including) the given path.
     */
    default Collection<TLeaf> getItemsUnder(TreePath<TNode> path)
    { return getBranchView(path).getItemsUnderRoot(); }

    /**
     * Gets all items in this tree whose paths start with (but not including) the given path, in order.
     * @param path The path to use to get items in this tree under.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of all the items in this tree whose paths start with (but not including) the given path, ordered
     *         by tree paths using the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TLeaf> getItemsUnderInOrder(TreePath<TNode> path, Comparator<? super TNode> comparator)
    { return getBranchView(path).getItemsUnderRootInOrder(comparator); }

    /**
     * Gets all items in this tree whose paths the given path starts with. (Including root)
     * @param path The path to use to get items in this tree along.
     * @return A list of all the items in this tree whose paths the given path starts with, (including root) in order
     *         from shortest to longest.
     */
    List<TLeaf> getItemsAlong(TreePath<TNode> path);

    /**
     * Gets all items in this tree whose paths the given path starts with. (not including root)
     * @param path The path to use to get items in this tree along.
     * @return A list of all the items in this tree whose paths the given path starts with, (not including root) in
     *         order from shortest to longest.
     */
    List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path);

    /**
     * Gets all items in this tree with single-element paths.
     * @return A collection of all the items in this tree with single-element paths.
     */
    Collection<TLeaf> getImmediateItems();

    /**
     * Gets all items in this tree with single-element paths, in order.
     * @param comparator The comparator to compare the single nodes of each of the items' paths.
     * @return A list of all the items in this tree with single-element paths, ordered by the single nodes in their
     *         paths.
     * @see TreePath#getComparator(Comparator)
     */
    List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets the item at root (if any) and all items in this tree with single-element paths.
     * @return A collection of all the items in this tree with single-element or zero-element (root) paths.
     */
    Collection<TLeaf> getRootAndImmediateItems();

    /**
     * Gets the item at root (if any) and all items in this tree with single-element paths, in order.
     * @param comparator The comparator to compare the single nodes of each of the items' paths.
     * @return A list of all the items in this tree with single-element or zero-element (root) paths, ordered by the
     *         single nodes in their paths, where root comes first.
     * @see TreePath#getComparator(Comparator)
     */
    List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets the items in this tree with paths that start with and are one element longer than the given path.
     * @param path The path to get the children of.
     * @return A collection of all the items in this tree whose paths start with and are one element longer than the
     *         given path.
     */
    default Collection<TLeaf> getImmediatelyUnder(TreePath<TNode> path)
    { return getBranchView(path).getImmediateItems(); }

    /**
     * Gets the items in this tree with paths that start with and are one element longer than the given path, in order.
     * @param path The path to get the children of.
     * @param comparator The comparator to compare the single additional nodes of each of the items' paths.
     * @return A list of all the items in this tree with paths starting with and one element longer than the given path,
     *         ordered by the single additional nodes in their paths.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TLeaf> getImmediatelyUnderInOrder(TreePath<TNode> path, Comparator<? super TNode> comparator)
    { return getBranchView(path).getImmediateItemsInOrder(comparator); }

    /**
     * Gets the items in this tree with paths that start with and are zero or one element longer than the given path.
     * @param path The path to get at and the children of.
     * @return A collection of all the items in this tree whose paths start with and are zero or one element longer than
     *         the given path.
     */
    default Collection<TLeaf> getAtAndImmediatelyUnder(TreePath<TNode> path)
    { return getBranchView(path).getRootAndImmediateItems(); }

    /**
     * Gets the items in this tree with paths that start with and are zero or one element longer than the given path, in
     * order.
     * @param path The path to get the children of.
     * @param comparator The comparator to compare the single additional nodes of each of the items' paths.
     * @return A list of all the items in this tree with paths starting with and zero or one element longer than the
     *         given path, ordered by the single additional nodes in their paths, where the item at the given path (if
     *         any) comes first.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TLeaf> getAtAndImmediatelyUnderInOrder(TreePath<TNode> path,
                                                        Comparator<? super TNode> comparator)
    { return getBranchView(path).getRootAndImmediateItemsInOrder(comparator); }
    //endregion

    //region get Entries

    /**
     * Gets entries for all items in this tree.
     * @return A collection of entries of all items in this tree.
     */
    Collection<TreeEntry<TNode, TLeaf>> getEntries();

    /**
     * Gets entries for all items in this tree, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of entries of all items in this tree, ordered by tree paths using the given comparator to compare
     * their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets entries for all items in this tree that adhere to the given predicate.
     * @param test The predicate that items and their paths must adhere to to be included in the returned collection.
     * @return A collection of entries for all items in this tree that match the given predicate.
     */
    Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<? super TreeEntry<TNode, TLeaf>> test);

    /**
     * Gets entries for all items in this tree that adhere to the given predicate.
     * @param test The predicate that items and their paths must adhere to to be included in the returned collection.
     * @return A collection of entries for all items in this tree that match the given predicate.
     */
    default Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(
            BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    { return getEntriesWhere(x -> test.test(x.path, x.item)); }

    /**
     * Gets entries for all items in this tree that adhere to the given predicate, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @param test The predicate that items and their paths must adhere to to be included in the returned collection.
     * @return A list of entries for all items in this tree that match the given predicate, ordered by tree paths using
     *         the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator,
                                                         Predicate<? super TreeEntry<TNode, TLeaf>> test);

    /**
     * Gets entries for all items in this tree that adhere to the given predicate, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @param test The predicate that items and their paths must adhere to to be included in the returned collection.
     * @return A list of entries for all items in this tree that match the given predicate, ordered by tree paths using
     *         the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(
            Comparator<? super TNode> comparator,
            BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    { return getEntriesInOrderWhere(comparator, x -> test.test(x.path, x.item)); }

    /**
     * Gets entries for all items in this tree whose paths adhere to the given predicate.
     * @param test The predicate that items' paths must adhere to to be included in the returned collection.
     * @return A collection of entries for all items in this tree whose paths match the given predicate.
     */
    Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<? super TreePath<TNode>> test);

    /**
     * Gets entries for all items in this tree whose paths adhere to the given predicate, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @param test The predicate that items' paths must adhere to to be included in the returned collection.
     * @return A collection of entries for all items in this tree whose paths match the given predicate, ordered by tree
     *         paths using the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator,
                                                             Predicate<? super TreePath<TNode>> test);

    /**
     * Gets entries for all items in this tree other than root.
     * @return A collection of entries for all items in this tree, except for root.
     */
    Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot();

    /**
     * Gets entries for all items in this tree other than root, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of entries for all items in this tree, except for root, ordered by tree paths using the given
     *         comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets entries for all items in this tree whose path starts with (including) the given path.
     * @param path The path to get entries for items at and under.
     * @return A collection of entries for all items in this tree whose paths start with (including) the given path.
     */
    default Collection<TreeEntry<TNode, TLeaf>> getEntriesAtAndUnder(TreePath<TNode> path)
    { return convertEntriesForTree(getBranchView(path).getEntries(), this, path); }

    /**
     * Gets entries for all items in this tree whose path starts with (including) the given path, in order.
     * @param path The path to get entries for items at and under.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of entries for all items in this tree whose paths start with (including) the given path, ordered
     *         by tree paths using the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TreeEntry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(TreePath<TNode> path,
                                                                      Comparator<? super TNode> comparator)
    { return convertEntriesForTree(getBranchView(path).getEntriesInOrder(comparator), this, path); }

    /**
     * Gets entries for all items in this tree whose path starts with (but not including) the given path.
     * @param path The path to get entries for items under.
     * @return A collection of entries for all items in this tree whose paths start with (but not including) the given
     *         path.
     */
    default Collection<TreeEntry<TNode, TLeaf>> getEntriesUnder(TreePath<TNode> path)
    { return convertEntriesForTree(getBranchView(path).getEntriesUnderRoot(), this, path); }

    /**
     * Gets entries for all items in this tree whose path starts with (but not including) the given path, in order.
     * @param path The path to get entries for items under.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of entries for all items in this tree whose paths start with (but not including) the given path,
     *         ordered by tree paths using the given comparator to compare their nodes.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TreeEntry<TNode, TLeaf>> getEntriesUnderInOrder(TreePath<TNode> path,
                                                                 Comparator<? super TNode> comparator)
    { return convertEntriesForTree(getBranchView(path).getEntriesUnderRootInOrder(comparator), this, path); }

    /**
     * Gets entries for all items in this tree whose paths the given path starts with. (Including root)
     * @param path The path to get entries for items along.
     * @return A list of entries for all items in this tree whose paths the given path starts with, (including root)
     *         ordered from shortest to longest.
     */
    List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path);

    /**
     * Gets entries for all items in this tree whose paths the given path starts with. (But not including root)
     * @param path The path to get entries for items along.
     * @return A list of entries for all items in this tree whose paths the given path starts with, (but not including
     *         root) ordered from shortest to longest.
     */
    List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path);

    /**
     * Gets entries for all items with single-element paths in this tree.
     * @return A collection of entries of all items with single-element paths in this tree.
     */
    Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries();

    /**
     * Gets entries for all items with single-element paths in this tree, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of entries of all items with single-element paths in this tree, ordered by the single nodes in
     *         their paths.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets entries for all items with zero-element (root) or single-element paths in this tree.
     * @return A collection of entries for all items with zero-element (root) or single-element paths in this tree.
     */
    Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries();

    /**
     * Gets entries for all items with zero-element (root) or single-element paths in this tree, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of entries of all items with zero-element or single-element paths in this tree, ordered by the
     *         single nodes in their paths, where root comes first.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator);

    /**
     * Gets entries for all items with paths beginning with and one element longer than the given path.
     * @param path The path to get entries for items immediately under.
     * @return A collection of entries for all items with paths beginning with and one element longer than the given
     *         path.
     */
    default Collection<TreeEntry<TNode, TLeaf>> getEntriesImmediatelyUnder(TreePath<TNode> path)
    { return convertEntriesForTree(getBranchView(path).getImmediateEntries(), this, path); }

    /**
     * Gets entries for all items with paths beginning with and one element longer than the given path, in order.
     * @param path The path to get entries for items immediately under.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A collection of entries for all items with paths beginning with and one element longer than the given
     *         path, ordered by the single nodes in their paths.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TreeEntry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(TreePath<TNode> path,
                                                                            Comparator<? super TNode> comparator)
    { return convertEntriesForTree(getBranchView(path).getImmediateEntriesInOrder(comparator), this, path); }

    /**
     * Gets entries for all items with paths beginning with and zero or one element longer than the given path.
     * @param path The path to get entries for items at and immediately under.
     * @return A collection of entries for all items with paths beginning with and zero or one element longer than the
     *         given path.
     */
    default Collection<TreeEntry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(TreePath<TNode> path)
    { return convertEntriesForTree(getBranchView(path).getRootAndImmediateEntries(), this, path); }

    /**
     * Gets entries for all items with paths beginning with and zero or one element longer than the given path.
     * @param path The path to get entries for items at and immediately under.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A collection of entries for all items with paths beginning with and zero or one element longer than the
     *         given path, ordered by the single nodes in their paths, where the entry for the item at the given path
     *         comes first.
     * @see TreePath#getComparator(Comparator)
     */
    default List<TreeEntry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(TreePath<TNode> path,
                                                                                 Comparator<? super TNode> comparator)
    { return convertEntriesForTree(getBranchView(path).getRootAndImmediateEntriesInOrder(comparator), this, path); }
    //endregion

    //region get branches

    /**
     * Gets the items in this tree starting with the given path, in a new tree with the given path removed from the
     * start of their paths.
     * @param path The path of the branch of this tree to get a copy of.
     * @return A new tree object populated with the items of this tree whose paths start with the given path, at their
     *         paths in this tree with the given path removed from the start.
     */
    Tree<TNode, TLeaf> getBranch(TreePath<TNode> path);

    /**
     * Gets all branches of this tree at single-element paths.
     * @return A map containing each branch (as might be returned from {@link #getBranch(TreePath)}) of this tree at a
     *         single-element path, mapped to the single element of that path.
     */
    Map<TNode, Tree<TNode, TLeaf>> getBranches();
    //endregion

    //region get paths

    /**
     * Gets the paths of every item in this tree.
     * @return A collection of the paths of all items in this tree.
     */
    Collection<TreePath<TNode>> getPaths();

    /**
     * Gets the paths of every item in this tree, in order.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A list of the paths of all items in this tree, ordered using the given comparator to compare each
     *         individual node of the paths.
     * @see TreePath#getComparator(Comparator)
     */
    List<TreePath<TNode>> getPathsInOrder(Comparator<? super TNode> comparator);
    //endregion

    //region get branch views

    /**
     * Gets a live view of this tree, treating all calls as though the given path is the root of the tree and all paths
     * branch from there.
     * @param path The path to get a branch view of this tree of.
     * @return A live view of this tree. All changes made to this tree are reflected in the returned tree and vice
     *         versa.
     */
    Tree<TNode, TLeaf> getBranchView(TreePath<TNode> path);

    /**
     * Gets live views of each of the branches at single-element paths of this tree, as might be returned by
     * {@link #getBranchView(TreePath)}.
     * @return A map containing views of each branch (as might be returned from {@link #getBranchView(TreePath)}) of
     *         this tree at a single-element path, mapped to the single element of that path.
     */
    Map<TNode, Tree<TNode, TLeaf>> getBranchViews();
    //endregion

    //region set items in tree

    /**
     * Sets the item at root in this tree to the given item.
     * @param newItem The item to be at root in this tree.
     * @return The previous item at root in this tree, or null if no item was previously at root in this tree. Note that
     *         this may also return null if the previous item at root in this tree was null.
     */
    TLeaf setRootItem(TLeaf newItem);

    /**
     * Sets the item at root in this tree to the given item if no item is currently set at root.
     * @param newItem The item to be at root in this tree.
     * @return The item already at root in this tree, or null if no item was already at root in this tree. Note that
     *         this may also return null if the previous item at root in this tree was null.
     */
    TLeaf setRootItemIfAbsent(TLeaf newItem);

    /**
     * Sets the item at root in this tree to the given item if the given predicate is adhered to.
     * @param newItem The item to be at root in this tree.
     * @param test The predicate that must be adhered to in order to set the root item of this tree to the given item.
     *             The path being set at and the item already present at the path (or null if no item was already
     *             present at root) are passed into the predicate. Note that this may also pass null as the item already
     *             at the given path if the item already at the given path was actually null.
     * @return The item already at root in this tree, or null if no item was already at root in this tree. Note that
     *         this may also return null if the previous item at root in this tree was null.
     */
    TLeaf setRootItemIf(TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    /**
     * Set the item at the given path to the given item.
     * @param path The path to set the item at.
     * @param newItem The item to set at the given path.
     * @return The item previously at the given path in this tree, or null if no item was previously at the given path.
     *         Note that this may also return null if the previous item at the given path in this tree was null.
     */
    TLeaf setAt(TreePath<TNode> path, TLeaf newItem);

    /**
     * Sets the item at the given path to the given item if there is not already an item at the given path.
     * @param path The path to set the item at.
     * @param newItem The item to set at the given path.
     * @return The item already at the given path in this tree, or null if no item was already at the given path. Note
     *         that this may also return null if the previous item at the given path in this tree was null.
     */
    TLeaf setAtIfAbsent(TreePath<TNode> path, TLeaf newItem);

    /**
     * Sets the item at the given path to the given item if the given predicate is adhered to.
     * @param path The path to set the item at.
     * @param newItem The item to set at the given path.
     * @param test The predicate that must be adhered to in order to set the item at the given path of this tree to the
     *             given item. The path being set at and the item already present at the path (or null if no item was
     *             already present at the given path) are passed into the predicate. Note that this may also pass null
     *             as the item already at the given path if the item already at the given path was actually null.
     * @return The item already at the given path in this tree, or null if no item was already at the given path. Note
     *         that this may also return null if the previous item at the given path in this tree was null.
     */
    TLeaf setAtIf(TreePath<TNode> path, TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);
    //endregion

    //region remove items in tree

    /**
     * Removes all items from this tree.
     */
    void clear();

    /**
     * Removes all items other than that of root from this tree.
     */
    void clearUnderRoot();

    /**
     * Removes all items with paths starting with (including) the given path from this tree.
     * @param path The path to remove items at and under.
     */
    default void clearAtAndUnder(TreePath<TNode> path)
    { getBranchView(path).clear(); }

    /**
     * Removes all items with paths starting but (but not including) the given path from this tree.
     * @param path The path to remove items under.
     */
    default void clearUnder(TreePath<TNode> path)
    { getBranchView(path).clearUnderRoot(); }

    /**
     * Removes all items that adhere to the given predicate from this tree.
     * @param test The predicate an item in this tree must adhere to to be removed from this tree. Each tree path and
     *             item at that path are passed into the predicate as arguments.
     */
    void clearWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    /**
     * Removes all items whose paths adhere to the given predicate from this tree.
     * @param test The predicate an item's path in this tree must adhere to to be removed from this tree. Each tree path
     *             and item at that path are passed into the predicate as arguments.
     */
    void clearWherePath(Predicate<? super TreePath<TNode>> test);

    /**
     * Removes the item at root.
     * @return The item at root, or null if there was no item at root. Note that this may also return null if the item
     *         at root was null.
     */
    TLeaf clearRoot();

    /**
     * Removes the item at root if the given predicate is adhered to.
     * @param test The predicate the root item must adhere to to be removed. The item's path and the item itself are
     *             passed into the predicate as arguments.
     * @return The item that was at root, or null if there was no item at root. Note that this may also return null if
     *         the item at root was null.
     */
    TLeaf clearRootIf(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    /**
     * Removes the item in this tree at the given path.
     * @param path The path to remove an item at.
     * @return The item that was at the given path, or null if there was no item at that path. Note that this may also
     *         return null if the item at the given path was null.
     */
    default TLeaf clearAt(TreePath<TNode> path)
    { return getBranchView(path).clearRoot(); }

    /**
     * Removes the item at the given path if the given predicate is adhered to.
     * @param path The path to remove an item at.
     * @param test The predicate the item at the given path must adhere to to be removed. The item's path and the item
     *             itself are passed into the predicate as arguments.
     * @return The item that was at the given path, or null if there was no item at that path. Note that this may also
     *         return null if the item at the given path was null.
     */
    TLeaf clearAtIf(TreePath<TNode> path, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);
    //endregion

    //region conversion

    /**
     * Gets all items in this tree as a collection.
     * @return The items in this tree as a collection.
     */
    default Collection<TLeaf> toCollection()
    { return this.getItems(); }

    /**
     * Gets all items in this tree as a list.
     * @return The items in this tree as a list.
     */
    List<TLeaf> toList();

    /**
     * Gets all items in this tree as a list, ordered by their paths.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return The items in this tree as a list, ordered by their tree paths.
     * @see TreePath#getComparator(Comparator)
     */
    List<TLeaf> toOrderedList(Comparator<? super TNode> comparator);

    /**
     * Gets all items in this tree paired with their tree paths in a map.
     * @return The items in this tree and their paths as a map.
     */
    Map<TreePath<TNode>, TLeaf> toMap();

    /**
     * Gets a copy of this tree, where the paths of all items are reversed.
     * @return A new tree object, with all the items of this tree, but at reversed paths.
     */
    Tree<TNode, TLeaf> withReversedKeys();
    //endregion

    //region stats

    /**
     * Gets the number of items in this tree.
     * @return The number of items in this tree.
     */
    int size();

    /**
     * Gets the maximum path length of any item in this tree.
     * @return the maximum path length of any item in this tree.
     */
    int countDepth();
    //endregion

    //region string representations

    /**
     * Gets a string representation of this tree.
     * @return A string representation of this tree.
     */
    default String toTreeString()
    { return toTreeString((o1, o2) -> 0); }

    /**
     * Gets a string representation of this tree, where elements are ordered according to their paths.
     * @param comparator The comparator for comparing individual nodes of the paths of this tree.
     * @return A string representation of this tree.
     * @see TreePath#getComparator(Comparator)
     */
    default String toTreeString(Comparator<? super TNode> comparator)
    {
        StringBuilder sb = new StringBuilder();

        if(hasRootItem())
            sb.append("[(root)] = ").append(Objects.toString(getRootItem(), "(null)"));
        else
            sb.append("[(root)]");

        for(TreeEntry<TNode, TLeaf> entry : getEntriesInOrder(comparator))
        {
            sb.append("\n[")
              .append(entry.getPath().toString())
              .append("] = ")
              .append(Objects.toString(entry.getItem(), "(null)"));
        }

        return sb.toString();
    }
    //endregion
}
