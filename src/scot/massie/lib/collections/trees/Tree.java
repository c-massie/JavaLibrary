package scot.massie.lib.collections.trees;

import scot.massie.lib.collections.trees.exceptions.NoItemAtPathException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface Tree<TNode, TLeaf>
{
    //region check state of contents
    //region has items
    boolean hasItems();

    boolean hasNonRootItems();

    default boolean hasItemsAtOrUnder(TreePath<TNode> path)
    { return getBranchView(path).hasItems(); }

    default boolean hasItemsUnder(TreePath<TNode> path)
    { return getBranchView(path).hasNonRootItems(); }

    boolean hasItemsAlong(TreePath<TNode> path);

    boolean hasNonRootItemsAlong(TreePath<TNode> path);

    boolean hasItemAt(TreePath<TNode> path);

    boolean hasRootItem();
    //endregion

    //region is empty
    default boolean isEmpty()
    { return !hasItems(); }

    default boolean isEmptyUnderRoot()
    { return !hasNonRootItems(); }

    default boolean isEmptyAtAndUnder(TreePath<TNode> path)
    { return !hasItemsAtOrUnder(path); }

    default boolean isEmptyUnder(TreePath<TNode> path)
    { return !hasItemsUnder(path); }

    default boolean isEmptyAlong(TreePath<TNode> path)
    { return !hasItemsAlong(path); }

    default boolean isEmptyAlongAfterRoot(TreePath<TNode> path)
    { return !hasNonRootItemsAlong(path); }

    default boolean isEmptyAt(TreePath<TNode> path)
    { return !hasItemAt(path); }

    default boolean isEmptyAtRoot()
    { return !hasRootItem(); }
    //endregion
    //endregion

    //region get stored items
    //region root
    TLeaf getRootItem() throws NoItemAtPathException;

    TLeaf getRootItemOr(TLeaf defaultItem);

    Object getRootItemOrDefaultOfAnyType(Object defaultItem);

    default TLeaf getRootItemOrNull()
    { return getRootItemOr(null); }
    //endregion

    //region leaves
    TLeaf getAt(TreePath<TNode> path) throws NoItemAtPathException;

    TLeaf getAtOr(TreePath<TNode> path, TLeaf defaultItem);

    Object getAtOrDefaultOfAnyType(TreePath<TNode> path, Object defaultItem);

    default TLeaf getAtOrNull(TreePath<TNode> path)
    { return getAtOr(path, null); }
    //endregion
    //endregion

    //region get multiple stored items
    Collection<TLeaf> getItems();

    List<TLeaf> getItemsInOrder(Comparator<? super TNode> comparator);

    Collection<TLeaf> getItemsWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    Collection<TLeaf> getItemsWherePath(Predicate<? super TreePath<TNode>> test);

    Collection<TLeaf> getItemsInOrderWhere(Comparator<? super TNode> comparator,
                                           BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    Collection<TLeaf> getItemsInOrderWherePath(Comparator<? super TNode> comparator,
                                               Predicate<? super TreePath<TNode>> test);

    Collection<TLeaf> getItemsUnderRoot();

    List<TLeaf> getItemsUnderRootInOrder(Comparator<? super TNode> comparator);

    default Collection<TLeaf> getItemsAtAndUnder(TreePath<TNode> path)
    { return getBranchView(path).getItems(); }

    default List<TLeaf> getItemsAtAndUnderInOrder(TreePath<TNode> path, Comparator<? super TNode> comparator)
    { return getBranchView(path).getItemsInOrder(comparator); }

    default Collection<TLeaf> getItemsUnder(TreePath<TNode> path)
    { return getBranchView(path).getItemsUnderRoot(); }

    default List<TLeaf> getItemsUnder(TreePath<TNode> path, Comparator<? super TNode> comparator)
    { return getBranchView(path).getItemsUnderRootInOrder(comparator); }

    List<TLeaf> getItemsAlong(TreePath<TNode> path);

    List<TLeaf> getItemsUnderRootAlong(TreePath<TNode> path);

    Collection<TLeaf> getImmediateItems();

    List<TLeaf> getImmediateItemsInOrder(Comparator<? super TNode> comparator);

    Collection<TLeaf> getRootAndImmediateItems();

    List<TLeaf> getRootAndImmediateItemsInOrder(Comparator<? super TNode> comparator);

    default Collection<TLeaf> getImmediatelyUnder(TreePath<TNode> path)
    { return getBranchView(path).getImmediateItems(); }

    default List<TLeaf> getImmediatelyUnderInOrder(TreePath<TNode> path, Comparator<? super TNode> comparator)
    { return getBranchView(path).getImmediateItemsInOrder(comparator); }

    default Collection<TLeaf> getAtAndImmediatelyUnder(TreePath<TNode> path)
    { return getBranchView(path).getRootAndImmediateItems(); }

    default List<TLeaf> getAtAndImmediatelyUnderInOrder(TreePath<TNode> path,
                                                        Comparator<? super TNode> comparator)
    { return getBranchView(path).getRootAndImmediateItemsInOrder(comparator); }
    //endregion

    //region get Entries
    Collection<TreeEntry<TNode, TLeaf>> getEntries();

    List<TreeEntry<TNode, TLeaf>> getEntriesInOrder(Comparator<? super TNode> comparator);

    Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(Predicate<? super TreeEntry<TNode, TLeaf>> test);

    default Collection<TreeEntry<TNode, TLeaf>> getEntriesWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test)
    { return getEntriesWhere(x -> test.test(x.path, x.item)); }

    List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWhere(Comparator<? super TNode> comparator,
                                                         Predicate<? super TreeEntry<TNode, TLeaf>> test);

    Collection<TreeEntry<TNode, TLeaf>> getEntriesWherePath(Predicate<? super TreePath<TNode>> test);

    List<TreeEntry<TNode, TLeaf>> getEntriesInOrderWherePath(Comparator<? super TNode> comparator,
                                                             Predicate<? super TreePath<TNode>> test);

    Collection<TreeEntry<TNode, TLeaf>> getEntriesUnderRoot();

    List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootInOrder(Comparator<? super TNode> comparator);

    default Collection<TreeEntry<TNode, TLeaf>> getEntriesAtAndUnder(TreePath<TNode> path)
    { return getBranchView(path).getEntries(); }

    default List<TreeEntry<TNode, TLeaf>> getEntriesAtAndUnderInOrder(TreePath<TNode> path,
                                                                      Comparator<? super TNode> comparator)
    { return getBranchView(path).getEntriesInOrder(comparator); }

    default Collection<TreeEntry<TNode, TLeaf>> getEntriesUnder(TreePath<TNode> path)
    { return getBranchView(path).getEntriesUnderRoot(); }

    default List<TreeEntry<TNode, TLeaf>> getEntriesUnderInOrder(TreePath<TNode> path,
                                                                 Comparator<? super TNode> comparator)
    { return getBranchView(path).getEntriesUnderRootInOrder(comparator); }

    List<TreeEntry<TNode, TLeaf>> getEntriesAlong(TreePath<TNode> path);

    List<TreeEntry<TNode, TLeaf>> getEntriesUnderRootAlong(TreePath<TNode> path);

    Collection<TreeEntry<TNode, TLeaf>> getImmediateEntries();

    List<TreeEntry<TNode, TLeaf>> getImmediateEntriesInOrder(Comparator<? super TNode> comparator);

    Collection<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntries();

    List<TreeEntry<TNode, TLeaf>> getRootAndImmediateEntriesInOrder(Comparator<? super TNode> comparator);

    default Collection<TreeEntry<TNode, TLeaf>> getEntriesImmediatelyUnder(TreePath<TNode> path)
    { return getBranchView(path).getImmediateEntries(); }

    default List<TreeEntry<TNode, TLeaf>> getEntriesImmediatelyUnderInOrder(TreePath<TNode> path,
                                                                            Comparator<? super TNode> comparator)
    { return getBranchView(path).getImmediateEntriesInOrder(comparator); }

    default Collection<TreeEntry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnder(TreePath<TNode> path)
    { return getBranchView(path).getRootAndImmediateEntries(); }

    default List<TreeEntry<TNode, TLeaf>> getEntriesAtAndImmediatelyUnderInOrder(TreePath<TNode> path,
                                                                                 Comparator<? super TNode> comparator)
    { return getBranchView(path).getRootAndImmediateEntriesInOrder(comparator); }
    //endregion

    //region get branches
    Tree<TNode, TLeaf> getBranch(TreePath<TNode> path);

    Map<TNode, Tree<TNode, TLeaf>> getBranches();
    //endregion

    //region get paths
    Collection<TreePath<TNode>> getPaths();

    List<TreePath<TNode>> getPathsInOrder(Comparator<? super TNode> comparator);
    //endregion

    //region get branch views
    Tree<TNode, TLeaf> getBranchView(TreePath<TNode> path);

    Map<TNode, Tree<TNode, TLeaf>> getBranchViews();
    //endregion

    //region set items in tree
    TLeaf setRootItem(TLeaf newItem);

    TLeaf setRootItemIfAbsent(TLeaf newItem);

    TLeaf setRootItemIf(TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    TLeaf setAt(TreePath<TNode> path, TLeaf newItem);

    TLeaf setAtIfAbsent(TreePath<TNode> path, TLeaf newItem);

    TLeaf setAtIf(TreePath<TNode> path, TLeaf newItem, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);
    //endregion

    //region remove items in tree
    void clear();

    void clearUnderRoot();

    default void clearAtAndUnder(TreePath<TNode> path)
    { getBranchView(path).clear(); }

    default void clearUnder(TreePath<TNode> path)
    { getBranchView(path).clearUnderRoot(); }

    void clearWhere(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    void clearWherePath(Predicate<? super TreePath<TNode>> test);

    TLeaf clearRoot();

    TLeaf clearRootIf(BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);

    default TLeaf clearAt(TreePath<TNode> path)
    { return getBranchView(path).clearRoot(); }

    TLeaf clearAtIf(TreePath<TNode> path, BiPredicate<? super TreePath<TNode>, ? super TLeaf> test);
    //endregion

    //region conversion
    default Collection<TLeaf> toCollection()
    { return this.getItems(); }

    List<TLeaf> toList();

    List<TLeaf> toOrderedList(Comparator<? super TNode> comparator);

    Tree<TNode, TLeaf> withReversedKeys();
    //endregion

    //region stats
    int size();

    int countDepth();
    //endregion

    //region string representations
    String toTreeString();
    //endregion
}
