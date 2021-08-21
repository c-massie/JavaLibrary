package scot.massie.lib.collections.trees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TreeUtils
{
    private TreeUtils()
    {}

    static <TNode, TLeaf> Collection<TreeEntry<TNode, TLeaf>> convertEntriesForTree(
            Iterable<? extends TreeEntry<TNode, TLeaf>> entries,
            Tree<TNode, TLeaf> sourceTree,
            TreePath<? extends TNode> pathPrefix)
    {
        Collection<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        for(TreeEntry<TNode, TLeaf> e : entries)
            result.add(new TreeEntry<>(sourceTree, e.path.prependedWith(pathPrefix), e.item));

        return result;
    }

    static <TNode, TLeaf> List<TreeEntry<TNode, TLeaf>> convertEntriesForTree(
            @SuppressWarnings("TypeMayBeWeakened") List<? extends TreeEntry<TNode, TLeaf>> entries,
            Tree<TNode, TLeaf> sourceTree,
            TreePath<? extends TNode> pathPrefix)
    {
        List<TreeEntry<TNode, TLeaf>> result = new ArrayList<>();

        for(TreeEntry<TNode, TLeaf> e : entries)
            result.add(new TreeEntry<>(sourceTree, e.path.prependedWith(pathPrefix), e.item));

        return result;
    }
}
