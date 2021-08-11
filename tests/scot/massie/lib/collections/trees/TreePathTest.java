package scot.massie.lib.collections.trees;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;


class TreePathTest
{
    /*

    getNodes
        Empty path (root)
        Single-element path
        Multi-element path
    getNode
        Multi-element path, index in range
        Multi-element path, index = 0
        Multi-element path, index = maximum
        Multi-element path, index = -1
        Multi-element path, index = maximum + 1
        Empty path (root), index = 0
        Empty path (root), index = -1
    size
        Multi-element path
        Single-element path
        Empty path (root)
    first
        Multi-element path
        Single-element path
        Empty path (root)
    firstOrNull
        Multi-element path
        Single-element path
        Empty path (root)
    last
        Multi-element path
        Single-element path
        Empty path (root)
    lastOrNull
        Multi-element path
        Single-element path
        Empty path (root)
    getParent
        Multi-element path
        Single-element path
        Empty path (root)
    truncateTo
        Multi-element path, longer than
        Multi-element path, same length
        Multi-element path, shorter than
        Multi-element path, single-element
        Multi-element path, zero-element (root)
        Multi-element path, negative
        Single-element path, longer than
        Single-element path, same length
        Single-element path, zero-element (root)
        Single-element path, negative
        Empty path (root), longer than
        Empty path (root), same length
        Empty path (root), negative
    withoutFirstNodes
        Multi-element path, more
        Multi-element path, all
        Multi-element path, some
        Multi-element path, one
        Multi-element path, none
        Multi-element path, negative
        Single-element path, more
        Single-element path, one
        Single-element path, none
        Single-element path, negative
        Empty path (root), more
        Empty path (root), none
        Empty path (root), negative
    withoutLastNodes
        Multi-element path, more
        Multi-element path, all
        Multi-element path, some
        Multi-element path, one
        Multi-element path, none
        Multi-element path, negative
        Single-element path, more
        Single-element path, one
        Single-element path, none
        Single-element path, negative
        Empty path (root), more
        Empty path (root), none
        Empty path (root), negative
    appendedWith(TreePath)
        null
        empty path (root)
        single-element path
        multi-element path
    appendedWith(TNode)
        null
        element
    prependedWith(TreePath)
        null
        empty path (root)
        single-element path
        multi-element path
    prependedWith(TNode)
        null
        element
    reversed
        Multi-element path
        Single-element path
        Empty path (root)
    isAncestorOf
        root, of root
        root, of multi-element path
        multi-element path, of null
        multi-element path, of root
        multi-element path, of beginning of path
        multi-element path, of same path
        multi-element path, of same path with additional elements
        multi-element path, of different shorter path
        multi-element path, of different same-length path
        multi-element path, of different longer path
    isEqualToOrAncestorOf
        root, of root
        root, of multi-element path
        multi-element path, of null
        multi-element path, of root
        multi-element path, of beginning of path
        multi-element path, of same path
        multi-element path, of same path with additional elements
        multi-element path, of different shorter path
        multi-element path, of different same-length path
        multi-element path, of different longer path
    isDescendantOf
        root, of root
        root, of multi-element path
        multi-element path, of null
        multi-element path, of root
        multi-element path, of beginning of path
        multi-element path, of same path
        multi-element path, of same path with additional elements
        multi-element path, of different shorter path
        multi-element path, of different same-length path
        multi-element path, of different longer path
    isEqualToOrDescendantOf
        root, of root
        root, of multi-element path
        multi-element path, of null
        multi-element path, of root
        multi-element path, of beginning of path
        multi-element path, of same path
        multi-element path, of same path with additional elements
        multi-element path, of different shorter path
        multi-element path, of different same-length path
        multi-element path, of different longer path
    isRoot
        root
        single-element path
        multi-element path
    equals(TreePath)
        root, to root
        root, to null
        root, to multi-element path path
        multi-element path, to null
        multi-element path, to root
        multi-element path, to beginning of path
        multi-element path, to same path
        multi-element path, to same path with additional elements
        multi-element path, to different shorter path
        multi-element path, to different same-length path
        multi-element path, to different longer path
    equals(Object)
        root, to root
        root, to null
        root, to multi-element path path
        multi-element path, to null
        multi-element path, to root
        multi-element path, to beginning of path
        multi-element path, to same path
        multi-element path, to same path with additional elements
        multi-element path, to different shorter path
        multi-element path, to different same-length path
        multi-element path, to different longer path
    toString
        root
        single-element path with null
        single-element path with non-null
        multi-element path, containing both null and non-null
    getComparableType
        all are comparable with same type
        all are comparable with different types
        none are comparable
        some are comparable with same type, some are not

     */

    /*

    Many tests except for the getNodes tests assumes that TreePath.getNodes works as expected.

    All tests assume that the constructor works as expected.

     */

    TreePath<String> getNewTreePath(String... elements)
    { return new TreePath<>(elements); }

    @Test
    void getNodes_empty()
    { assertThat(getNewTreePath().getNodes()).isEmpty(); }

    @Test
    void getNodes_single()
    { assertThat(getNewTreePath("doot").getNodes()).containsExactly("doot"); }

    @Test
    void getNodes_multi()
    { assertThat(getNewTreePath("first", "second", "third").getNodes()).containsExactly("first", "second", "third"); }

    @Test
    void getNode_multi_inRange()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").getNode(2)).isEqualTo("third"); }

    @Test
    void getNode_multi_min()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").getNode(0)).isEqualTo("first"); }

    @Test
    void getNode_multi_max()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").getNode(4)).isEqualTo("fifth"); }

    @Test
    void getNode_multi_underMin()
    {
        assertThrows(IndexOutOfBoundsException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").getNode(-1));
    }

    @Test
    void getNode_multi_overMax()
    {
        assertThrows(IndexOutOfBoundsException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").getNode(5));
    }

    @Test
    void getNode_root_zero()
    { assertThrows(IndexOutOfBoundsException.class, () -> getNewTreePath().getNode(0)); }

    @Test
    void getNode_root_underZero()
    { assertThrows(IndexOutOfBoundsException.class, () -> getNewTreePath().getNode(-1)); }

    @Test
    void size_multi()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").size()).isEqualTo(5); }

    @Test
    void size_single()
    { assertThat(getNewTreePath("first").size()).isEqualTo(1); }

    @Test
    void size_root()
    { assertThat(getNewTreePath().size()).isEqualTo(0); }

    @Test
    void first_multi()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").first()).isEqualTo("first"); }

    @Test
    void first_single()
    { assertThat(getNewTreePath("first").first()).isEqualTo("first"); }

    @Test
    void first_root()
    {
        assertThrows(NoSuchElementException.class,
                     () -> getNewTreePath().first());
    }

    @Test
    void firstOrNull_multi()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").firstOrNull()).isEqualTo("first"); }

    @Test
    void firstOrNull_single()
    { assertThat(getNewTreePath("first").firstOrNull()).isEqualTo("first"); }

    @Test
    void firstOrNull_root()
    { assertThat(getNewTreePath().firstOrNull()).isEqualTo(null); }

    @Test
    void last_multi()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").last()).isEqualTo("fifth"); }

    @Test
    void last_single()
    { assertThat(getNewTreePath("first").last()).isEqualTo("first"); }

    @Test
    void last_root()
    {
        assertThrows(NoSuchElementException.class,
                     () -> getNewTreePath().last());
    }

    @Test
    void lastOrNull_multi()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").lastOrNull()).isEqualTo("fifth"); }

    @Test
    void lastOrNull_single()
    { assertThat(getNewTreePath("first").lastOrNull()).isEqualTo("first"); }

    @Test
    void lastOrNull_root()
    { assertThat(getNewTreePath().lastOrNull()).isEqualTo(null); }

    @Test
    void getParent_multi()
    {
        //noinspection ConstantConditions It will not
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").getParent().getNodes())
                .containsExactly("first", "second", "third", "fourth");
    }

    @Test
    void getParent_single()
    {
        //noinspection ConstantConditions It will not
        assertThat(getNewTreePath("first").getParent().getNodes())
                .containsExactly();
    }

    @Test
    void getParent_root()
    { assertThat(getNewTreePath().getParent()).isNull(); }

    @Test
    void truncateTo_multi_longer()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").truncateTo(6).getNodes())
                .containsExactly("first", "second", "third", "fourth", "fifth");
    }

    @Test
    void truncateTo_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").truncateTo(5).getNodes())
                .containsExactly("first", "second", "third", "fourth", "fifth");
    }

    @Test
    void truncateTo_multi_shorter()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").truncateTo(4).getNodes())
                .containsExactly("first", "second", "third", "fourth");
    }

    @Test
    void truncateTo_multi_one()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").truncateTo(1).getNodes())
                .containsExactly("first");
    }

    @Test
    void truncateTo_multi_zero()
    { assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").truncateTo(0).getNodes()).isEmpty(); }

    @Test
    void truncateTo_multi_negative()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").truncateTo(-1));
    }

    @Test
    void truncateTo_single_longer()
    { assertThat(getNewTreePath("first").truncateTo(2).getNodes()).containsExactly("first"); }

    @Test
    void truncateTo_single_same()
    { assertThat(getNewTreePath("first").truncateTo(1).getNodes()).containsExactly("first"); }

    @Test
    void truncateTo_single_zero()
    { assertThat(getNewTreePath("first").truncateTo(0).getNodes()).isEmpty(); }

    @Test
    void truncateTo_single_negative()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath("first").truncateTo(-1)); }

    @Test
    void truncateTo_root_longer()
    { assertThat(getNewTreePath().truncateTo(1).getNodes()).isEmpty(); }

    @Test
    void truncateTo_root_same()
    { assertThat(getNewTreePath().truncateTo(0).getNodes()).isEmpty(); }

    @Test
    void truncateTo_root_negative()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath().truncateTo(-1)); }

    @Test
    void withoutFirstNodes_multi_more()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_multi_all()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_multi_some()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_multi_one()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_multi_none()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_multi_negative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_single_more()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_single_all()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_single_none()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_single_negative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_root_more()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_root_all()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutFirstNodes_root_negative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }



    @Test
    void withoutLastNodes_multi_more()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_multi_all()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_multi_some()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_multi_one()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_multi_none()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_multi_negative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_single_more()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_single_all()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_single_none()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_single_negative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_root_more()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_root_all()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void withoutLastNodes_root_negative()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    
    @Test
    void appendedWith_path_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void appendedWith_path_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void appendedWith_path_singleElement()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void appendedWith_path_multiElement()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void appendedWith_node_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void appendedWith_node_element()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void prependedWith_path_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void prependedWith_path_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void prependedWith_path_singleElement()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void prependedWith_path_multiElement()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void prependedWith_node_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void prependedWith_node_element()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void reversed_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void reversed_single()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void reversed_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_root_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_root_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
    
    @Test
    void isAncestorOf_multi_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_beginningPart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_sameWithAdditional()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_shorterAndDiffrent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_sameLengthButDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isAncestorOf_multi_longerAndDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_root_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_root_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_beginningPart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_sameWithAdditional()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_shorterAndDiffrent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_sameLengthButDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrAncestorOf_multi_longerAndDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_root_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_root_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_beginningPart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_sameWithAdditional()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_shorterAndDiffrent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_sameLengthButDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isDescendantOf_multi_longerAndDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_root_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_root_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_beginningPart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_sameWithAdditional()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_shorterAndDiffrent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_sameLengthButDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isEqualToOrDescendantOf_multi_longerAndDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isRoot_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isRoot_single()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void isRoot_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_root_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_root_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_beginningPart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_sameWithAdditional()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_shorterAndDiffrent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_sameLengthButDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_path_multi_longerAndDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_root_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_root_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_beginningPart()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_sameWithAdditional()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_shorterAndDiffrent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_sameLengthButDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void equals_object_multi_longerAndDifferent()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void toString_root()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void toString_single_null()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void toString_single_nonNull()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void toString_multi()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getComparableType_all_same()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getComparableType_all_different()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getComparableType_none()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }

    @Test
    void getComparableType_some()
    {
        // TO DO: Write.
        System.out.println("Test not yet written.");
    }
}