package scot.massie.lib.collections.trees;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

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

    TreePath<Object> getNewObjTreePath(Object... elements)
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
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").withoutFirstNodes(6));
    }

    @Test
    void withoutFirstNodes_multi_all()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutFirstNodes(5).getNodes())
                .isEmpty();
    }

    @Test
    void withoutFirstNodes_multi_some()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutFirstNodes(4).getNodes())
                .containsExactly("fifth");
    }

    @Test
    void withoutFirstNodes_multi_one()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutFirstNodes(1).getNodes())
                .containsExactly("second", "third", "fourth", "fifth");
    }

    @Test
    void withoutFirstNodes_multi_none()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutFirstNodes(0).getNodes())
                .containsExactly("first", "second", "third", "fourth", "fifth");
    }

    @Test
    void withoutFirstNodes_multi_negative()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").withoutFirstNodes(-1));
    }

    @Test
    void withoutFirstNodes_single_more()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath("first").withoutFirstNodes(2)); }

    @Test
    void withoutFirstNodes_single_all()
    { assertThat(getNewTreePath("first").withoutFirstNodes(1).getNodes()).isEmpty(); }

    @Test
    void withoutFirstNodes_single_none()
    { assertThat(getNewTreePath("first").withoutFirstNodes(0).getNodes()).containsExactly("first"); }

    @Test
    void withoutFirstNodes_single_negative()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath("first").withoutFirstNodes(-1)); }

    @Test
    void withoutFirstNodes_root_more()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath().withoutFirstNodes(1)); }

    @Test
    void withoutFirstNodes_root_all()
    { assertThat(getNewTreePath().withoutFirstNodes(0).getNodes()).isEmpty(); }

    @Test
    void withoutFirstNodes_root_negative()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath().withoutFirstNodes(-1)); }

    @Test
    void withoutLastNodes_multi_more()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").withoutLastNodes(6));
    }

    @Test
    void withoutLastNodes_multi_all()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutLastNodes(5).getNodes())
                .isEmpty();
    }

    @Test
    void withoutLastNodes_multi_some()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutLastNodes(4).getNodes())
                .containsExactly("first");
    }

    @Test
    void withoutLastNodes_multi_one()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutLastNodes(1).getNodes())
                .containsExactly("first", "second", "third", "fourth");
    }

    @Test
    void withoutLastNodes_multi_none()
    {
        assertThat(getNewTreePath("first", "second", "third", "fourth", "fifth").withoutLastNodes(0).getNodes())
                .containsExactly("first", "second", "third", "fourth", "fifth");
    }

    @Test
    void withoutLastNodes_multi_negative()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third", "fourth", "fifth").withoutLastNodes(-1));
    }

    @Test
    void withoutLastNodes_single_more()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath("first").withoutLastNodes(2)); }

    @Test
    void withoutLastNodes_single_all()
    { assertThat(getNewTreePath("first").withoutLastNodes(1).getNodes()).isEmpty(); }

    @Test
    void withoutLastNodes_single_none()
    { assertThat(getNewTreePath("first").withoutLastNodes(0).getNodes()).containsExactly("first"); }

    @Test
    void withoutLastNodes_single_negative()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath("first").withoutLastNodes(-1)); }

    @Test
    void withoutLastNodes_root_more()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath().withoutLastNodes(1)); }

    @Test
    void withoutLastNodes_root_all()
    { assertThat(getNewTreePath().withoutLastNodes(0).getNodes()).isEmpty(); }

    @Test
    void withoutLastNodes_root_negative()
    { assertThrows(IllegalArgumentException.class, () -> getNewTreePath().withoutLastNodes(-1)); }
    
    @Test
    void appendedWith_path_null()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third").appendedWith((TreePath<String>)null));
    }

    @Test
    void appendedWith_path_root()
    {
        assertThat(getNewTreePath("first", "second", "third").appendedWith(getNewTreePath()).getNodes())
                .containsExactly("first", "second", "third");
    }

    @Test
    void appendedWith_path_singleElement()
    {
        assertThat(getNewTreePath("first", "second", "third").appendedWith(getNewTreePath("fourth")).getNodes())
                .containsExactly("first", "second", "third", "fourth");
    }

    @Test
    void appendedWith_path_multiElement()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .appendedWith(getNewTreePath("fourth", "fifth", "sixth"))
                           .getNodes())
                .containsExactly("first", "second", "third", "fourth", "fifth", "sixth");
    }

    @Test
    void appendedWith_node_null()
    {
        assertThat(getNewTreePath("first", "second", "third").appendedWith((String)null).getNodes())
                .containsExactly("first", "second", "third", null);
    }

    @Test
    void appendedWith_node_element()
    {
        assertThat(getNewTreePath("first", "second", "third").appendedWith("fourth").getNodes())
                .containsExactly("first", "second", "third", "fourth");
    }

    @Test
    void prependedWith_path_null()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third").prependedWith((TreePath<String>)null));
    }

    @Test
    void prependedWith_path_root()
    {
        assertThat(getNewTreePath("first", "second", "third").prependedWith(getNewTreePath()).getNodes())
                .containsExactly("first", "second", "third");
    }

    @Test
    void prependedWith_path_singleElement()
    {
        assertThat(getNewTreePath("first", "second", "third").prependedWith(getNewTreePath("zeroth")).getNodes())
                .containsExactly("zeroth", "first", "second", "third");
    }

    @Test
    void prependedWith_path_multiElement()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .prependedWith(getNewTreePath("minustwoth", "minusoneth", "zeroth"))
                           .getNodes())
                .containsExactly("minustwoth", "minusoneth", "zeroth", "first", "second", "third");
    }

    @Test
    void prependedWith_node_null()
    {
        assertThat(getNewTreePath("first", "second", "third").prependedWith((String)null).getNodes())
                .containsExactly(null, "first", "second", "third");
    }

    @Test
    void prependedWith_node_element()
    {
        assertThat(getNewTreePath("first", "second", "third").prependedWith("zeroth").getNodes())
                .containsExactly("zeroth", "first", "second", "third");
    }

    @Test
    void reversed_multi()
    {
        assertThat(getNewTreePath("first", "second", "third").reversed().getNodes())
                .containsExactly("third", "second", "firth");
    }

    @Test
    void reversed_single()
    { assertThat(getNewTreePath("first").reversed().getNodes()).containsExactly("first"); }

    @Test
    void reversed_root()
    { assertThat(getNewTreePath().reversed().getNodes()).isEmpty(); }

    @Test
    void isAncestorOf_root_root()
    { assertThat(getNewTreePath().isAncestorOf(getNewTreePath())).isFalse(); }

    @Test
    void isAncestorOf_root_multi()
    { assertThat(getNewTreePath().isAncestorOf(getNewTreePath("first", "second", "third"))).isTrue(); }
    
    @Test
    void isAncestorOf_multi_null()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third").isAncestorOf(null));
    }

    @Test
    void isAncestorOf_multi_root()
    { assertThat(getNewTreePath("first", "second", "third").isAncestorOf(getNewTreePath())).isFalse(); }

    @Test
    void isAncestorOf_multi_beginningPart()
    { assertThat(getNewTreePath("first", "second", "third").isAncestorOf(getNewTreePath("first"))).isFalse(); }

    @Test
    void isAncestorOf_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third").isAncestorOf(getNewTreePath("first", "second", "third")))
                .isFalse();
    }

    @Test
    void isAncestorOf_multi_sameWithAdditional()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isAncestorOf(getNewTreePath("first", "second", "third", "fourth")))
                .isTrue();
    }

    @Test
    void isAncestorOf_multi_shorterAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third").isAncestorOf(getNewTreePath("oneth")))
                .isFalse();
    }

    @Test
    void isAncestorOf_multi_sameLengthButDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third").isAncestorOf(getNewTreePath("oneth", "twoth", "threeth")))
                .isFalse();
    }

    @Test
    void isAncestorOf_multi_longerAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isAncestorOf(getNewTreePath("oneth", "twoth", "threeth", "fourst")))
                .isFalse();
    }

    @Test
    void isEqualToOrAncestorOf_root_root()
    { assertThat(getNewTreePath().isEqualToOrAncestorOf(getNewTreePath())).isTrue(); }

    @Test
    void isEqualToOrAncestorOf_root_multi()
    { assertThat(getNewTreePath().isEqualToOrAncestorOf(getNewTreePath("first", "second", "third"))).isTrue(); }

    @Test
    void isEqualToOrAncestorOf_multi_null()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third").isEqualToOrAncestorOf(null));
    }

    @Test
    void isEqualToOrAncestorOf_multi_root()
    { assertThat(getNewTreePath("first", "second", "third").isEqualToOrAncestorOf(getNewTreePath())).isFalse(); }

    @Test
    void isEqualToOrAncestorOf_multi_beginningPart()
    { assertThat(getNewTreePath("first", "second", "third").isEqualToOrAncestorOf(getNewTreePath("first"))).isFalse(); }

    @Test
    void isEqualToOrAncestorOf_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrAncestorOf(getNewTreePath("first", "second", "third")))
                .isTrue();
    }

    @Test
    void isEqualToOrAncestorOf_multi_sameWithAdditional()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrAncestorOf(getNewTreePath("first", "second", "third", "fourth")))
                .isTrue();
    }

    @Test
    void isEqualToOrAncestorOf_multi_shorterAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third").isEqualToOrAncestorOf(getNewTreePath("oneth")))
                .isFalse();
    }

    @Test
    void isEqualToOrAncestorOf_multi_sameLengthButDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrAncestorOf(getNewTreePath("oneth", "twoth", "threeth")))
                .isFalse();
    }

    @Test
    void isEqualToOrAncestorOf_multi_longerAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrAncestorOf(getNewTreePath("oneth", "twoth", "threeth", "fourst")))
                .isFalse();
    }

    @Test
    void isDescendantOf_root_root()
    { assertThat(getNewTreePath().isDescendantOf(getNewTreePath())).isFalse(); }

    @Test
    void isDescendantOf_root_multi()
    { assertThat(getNewTreePath().isDescendantOf(getNewTreePath("first", "second", "third"))).isFalse(); }

    @Test
    void isDescendantOf_multi_null()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third").isDescendantOf(null));
    }

    @Test
    void isDescendantOf_multi_root()
    { assertThat(getNewTreePath("first", "second", "third").isDescendantOf(getNewTreePath())).isTrue(); }

    @Test
    void isDescendantOf_multi_beginningPart()
    { assertThat(getNewTreePath("first", "second", "third").isDescendantOf(getNewTreePath("first"))).isTrue(); }

    @Test
    void isDescendantOf_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isDescendantOf(getNewTreePath("first", "second", "third")))
                .isFalse();
    }

    @Test
    void isDescendantOf_multi_sameWithAdditional()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isDescendantOf(getNewTreePath("first", "second", "third", "fourth")))
                .isFalse();
    }

    @Test
    void isDescendantOf_multi_shorterAndDifferent()
    { assertThat(getNewTreePath("first", "second", "third").isDescendantOf(getNewTreePath("oneth"))).isFalse(); }

    @Test
    void isDescendantOf_multi_sameLengthButDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isDescendantOf(getNewTreePath("oneth", "twoth", "threeth")))
                .isFalse();
    }

    @Test
    void isDescendantOf_multi_longerAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isDescendantOf(getNewTreePath("oneth", "twoth", "threeth", "fourst")))
                .isFalse();
    }

    @Test
    void isEqualToOrDescendantOf_root_root()
    { assertThat(getNewTreePath().isEqualToOrDescendantOf(getNewTreePath())).isTrue(); }

    @Test
    void isEqualToOrDescendantOf_root_multi()
    { assertThat(getNewTreePath().isEqualToOrDescendantOf(getNewTreePath("first", "second", "third"))).isFalse(); }

    @Test
    void isEqualToOrDescendantOf_multi_null()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> getNewTreePath("first", "second", "third").isEqualToOrDescendantOf(null));
    }

    @Test
    void isEqualToOrDescendantOf_multi_root()
    { assertThat(getNewTreePath("first", "second", "third").isEqualToOrDescendantOf(getNewTreePath())).isTrue(); }

    @Test
    void isEqualToOrDescendantOf_multi_beginningPart()
    {
        assertThat(getNewTreePath("first", "second", "third").isEqualToOrDescendantOf(getNewTreePath("first")))
                .isTrue();
    }

    @Test
    void isEqualToOrDescendantOf_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrDescendantOf(getNewTreePath("first", "second", "third")))
                .isTrue();
    }

    @Test
    void isEqualToOrDescendantOf_multi_sameWithAdditional()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrDescendantOf(getNewTreePath("first", "second", "third", "fourth")))
                .isFalse();
    }

    @Test
    void isEqualToOrDescendantOf_multi_shorterAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrDescendantOf(getNewTreePath("oneth")))
                .isFalse();
    }

    @Test
    void isEqualToOrDescendantOf_multi_sameLengthButDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrDescendantOf(getNewTreePath("oneth", "twoth", "threeth")))
                .isFalse();
    }

    @Test
    void isEqualToOrDescendantOf_multi_longerAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .isEqualToOrDescendantOf(getNewTreePath("oneth", "twoth", "threeth", "fourst")))
                .isFalse();
    }

    @Test
    void isRoot_root()
    { assertThat(getNewTreePath().isRoot()).isTrue(); }

    @Test
    void isRoot_single()
    { assertThat(getNewTreePath("first").isRoot()).isFalse(); }

    @Test
    void isRoot_multi()
    { assertThat(getNewTreePath("first", "second", "third").isRoot()).isFalse(); }

    @Test
    void equals_path_root_root()
    { assertThat(getNewTreePath().equals((TreePath<String>)getNewTreePath())).isTrue(); }

    @Test
    void equals_path_root_multi()
    { assertThat(getNewTreePath().equals((TreePath<String>)getNewTreePath("first", "second", "third"))).isFalse(); }

    @Test
    void equals_path_multi_null()
    { assertThat(getNewTreePath("first", "second", "third").equals((TreePath<String>)null)).isFalse(); }

    @Test
    void equals_path_multi_root()
    { assertThat(getNewTreePath("first", "second", "third").equals((TreePath<String>)getNewTreePath())).isFalse(); }

    @Test
    void equals_path_multi_beginningPart()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((TreePath<String>)getNewTreePath("first")))
                .isFalse();
    }

    @Test
    void equals_path_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((TreePath<String>)getNewTreePath("first", "second", "third")))
                .isTrue();
    }

    @Test
    void equals_path_multi_sameWithAdditional()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((TreePath<String>)getNewTreePath("first", "second", "third", "fourth")))
                .isFalse();
    }

    @Test
    void equals_path_multi_shorterAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((TreePath<String>)getNewTreePath("oneth")))
                .isFalse();
    }

    @Test
    void equals_path_multi_sameLengthButDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((TreePath<String>)getNewTreePath("oneth", "twoth", "threeth")))
                .isFalse();
    }

    @Test
    void equals_path_multi_longerAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((TreePath<String>)getNewTreePath("oneth", "twoth", "threeth", "fourst")))
                .isFalse();
    }

    @Test
    void equals_object_root_root()
    { assertThat(getNewTreePath().equals((Object)getNewTreePath())).isTrue(); }

    @Test
    void equals_object_root_multi()
    { assertThat(getNewTreePath().equals((Object)getNewTreePath("first", "second", "third"))).isFalse(); }

    @Test
    void equals_object_multi_null()
    { assertThat(getNewTreePath("first", "second", "third").equals((Object)null)).isFalse(); }

    @Test
    void equals_object_multi_root()
    { assertThat(getNewTreePath("first", "second", "third").equals((Object)getNewTreePath())).isFalse(); }

    @Test
    void equals_object_multi_beginningPart()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((Object)getNewTreePath("first")))
                .isFalse();
    }

    @Test
    void equals_object_multi_same()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((Object)getNewTreePath("first", "second", "third")))
                .isTrue();
    }

    @Test
    void equals_object_multi_sameWithAdditional()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((Object)getNewTreePath("first", "second", "third", "fourth")))
                .isFalse();
    }

    @Test
    void equals_object_multi_shorterAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((Object)getNewTreePath("oneth")))
                .isFalse();
    }


    @Test
    void equals_object_multi_sameLengthButDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((Object)getNewTreePath("oneth", "twoth", "threeth")))
                .isFalse();
    }

    @Test
    void equals_object_multi_longerAndDifferent()
    {
        assertThat(getNewTreePath("first", "second", "third")
                           .equals((Object)getNewTreePath("oneth", "twoth", "threeth", "fourst")))
                .isFalse();
    }

    @Test
    void toString_root()
    { assertThat(getNewTreePath().toString()).isEqualTo("(root)"); }

    @Test
    void toString_single_null()
    { assertThat(getNewTreePath((String)null).toString()).isEqualTo("(null)"); }

    @Test
    void toString_single_nonNull()
    { assertThat(getNewTreePath("first").toString()).isEqualTo("first"); }

    @Test
    void toString_multi()
    { assertThat(getNewTreePath("first", "second", "third").toString()).isEqualTo("first.second.third"); }

    @Test
    void getComparableType_all_same()
    { assertThat(getNewTreePath("first", "second", "third").getComparableType()).isEqualTo(String.class); }

    @Test
    void getComparableType_all_different()
    { assertThat(getNewObjTreePath("first", 2, UUID.randomUUID()).getComparableType()).isNull(); }

    @Test
    void getComparableType_none()
    {
        assertThat(getNewObjTreePath(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>())
                           .getComparableType())
                .isNull();
    }

    @Test
    void getComparableType_some()
    {
        assertThat(getNewObjTreePath("first", "second", new ArrayList<String>(), new ArrayList<String>())
                           .getComparableType())
                .isNull();
    }
}