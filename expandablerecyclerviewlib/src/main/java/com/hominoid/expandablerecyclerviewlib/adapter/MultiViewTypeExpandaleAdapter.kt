package com.hominoid.expandablerecyclerviewlib.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hominoid.expandablerecyclerviewlib.models.ExpandableListItem
import com.hominoid.expandablerecyclerviewlib.models.ExpandableListPosition
import com.hominoid.expandablerecyclerviewlib.viewholders.ChildViewHolder
import com.hominoid.expandablerecyclerviewlib.viewholders.GroupViewHolder

abstract class MultiViewTypeExpandaleAdapter<GVH : GroupViewHolder, CVH : ChildViewHolder>(groups: List<ExpandableListItem<*, *>>) :
    ExpandableRecyclerViewAdapter<GVH, CVH>(groups) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (isGroup(viewType)) {
            val gvh = onCreateGroupViewHolder(parent, viewType)
            gvh.setOnGroupClickListener(this)
            return gvh
        } else if (isChild(viewType)) {
            return onCreateChildViewHolder(parent, viewType)
        }
        throw IllegalArgumentException("viewType is not valid")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listPos: ExpandableListPosition = expandableList.getUnflattenedPosition(position)
        val group: ExpandableListItem<*, *> = expandableList.getExpandableGroup(listPos)
        if (isGroup(getItemViewType(position))) {
            onBindGroupViewHolder(holder as GVH, position, group)
        } else if (isChild(getItemViewType(position))) {
            onBindChildViewHolder(holder as CVH, position, group, listPos.childPos)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val listPosition: ExpandableListPosition = expandableList.getUnflattenedPosition(position)
        val group: ExpandableListItem<*, *> = expandableList.getExpandableGroup(listPosition)
        val viewType: Int = listPosition.type
        return when (viewType) {
            ExpandableListPosition.GROUP -> getGroupViewType(position, group)
            ExpandableListPosition.CHILD -> getChildViewType(
                position,
                group,
                listPosition.childPos
            )
            else -> viewType
        }
    }

    open fun getChildViewType(position: Int, group: ExpandableListItem<*, *>, childIndex: Int): Int {
        return super.getItemViewType(position)
    }

    open fun getGroupViewType(position: Int, group: ExpandableListItem<*, *>): Int {
        return super.getItemViewType(position)
    }

    open fun isGroup(viewType: Int): Boolean {
        return viewType == ExpandableListPosition.GROUP
    }

    open fun isChild(viewType: Int): Boolean {
        return viewType == ExpandableListPosition.CHILD
    }
}