package com.hominoid.expandablerecyclerviewlib.adapter

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hominoid.expandablerecyclerviewlib.listener.ExpandCollapseListener
import com.hominoid.expandablerecyclerviewlib.listener.ExpandableListItemClickListener
import com.hominoid.expandablerecyclerviewlib.listener.GroupExpandCollapseListener
import com.hominoid.expandablerecyclerviewlib.listener.OnGroupClickListener
import com.hominoid.expandablerecyclerviewlib.models.ExpandableList
import com.hominoid.expandablerecyclerviewlib.models.ExpandableListItem
import com.hominoid.expandablerecyclerviewlib.models.ExpandableListPosition
import com.hominoid.expandablerecyclerviewlib.utils.ExpandCollapseController
import com.hominoid.expandablerecyclerviewlib.viewholders.ChildViewHolder
import com.hominoid.expandablerecyclerviewlib.viewholders.GroupViewHolder

abstract class ExpandableRecyclerViewAdapter<GVH : GroupViewHolder, CVH : ChildViewHolder>(
    groups: List<ExpandableListItem<*, *>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ExpandCollapseListener,
    OnGroupClickListener {

    protected var expandableList: ExpandableList
    private val expandCollapseController: ExpandCollapseController
    private var groupClickListener: OnGroupClickListener? =
        null
    var listener: ExpandableListItemClickListener? = null
    private var expandCollapseListener: GroupExpandCollapseListener? = null

    override fun getItemViewType(position: Int): Int {
        return expandableList.getUnflattenedPosition(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ExpandableListPosition.GROUP -> {
                val gvh = onCreateGroupViewHolder(parent, viewType)
                gvh!!.setOnGroupClickListener(this)
                gvh
            }
            ExpandableListPosition.CHILD -> {
                val cvh = onCreateChildViewHolder(parent, viewType)
                cvh!!.setOnChildClickListener(this)
                cvh
            }
            else -> throw IllegalArgumentException("viewType is not valid")
        }
    }

    abstract fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): GVH

    abstract fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): CVH

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listPos = expandableList.getUnflattenedPosition(position)
        val group = expandableList.getExpandableGroup(listPos)
        when (listPos.type) {
            ExpandableListPosition.GROUP -> {
                onBindGroupViewHolder(holder as GVH, position, group)
                if (isGroupExpanded(group)) {
                    (holder as GVH)!!.expand()
                } else {
                    (holder as GVH)!!.collapse()
                }
            }
            ExpandableListPosition.CHILD -> onBindChildViewHolder(
                holder as CVH,
                position,
                group,
                listPos.childPos
            )
        }
    }

    abstract fun onBindGroupViewHolder(
        holder: GVH,
        flatPosition: Int,
        group: ExpandableListItem<*, *>
    )

    abstract fun onBindChildViewHolder(
        holder: CVH, flatPosition: Int, group: ExpandableListItem<*, *>,
        childIndex: Int
    )

    val groups: List<ExpandableListItem<*, *>>
        get() = expandableList.groups

    override fun getItemCount(): Int {
        return expandableList.visibleItemCount
    }

    override fun onGroupExpanded(
        positionStart: Int,
        itemCount: Int
    ) { //update header
        val headerPosition = positionStart - 1
        notifyItemChanged(headerPosition)
        // only insert if there items to insert
        if (itemCount > 0) {
            notifyItemRangeInserted(positionStart, itemCount)
            if (expandCollapseListener != null) {
                val groupIndex = expandableList.getUnflattenedPosition(positionStart).groupPos
                expandCollapseListener!!.onGroupExpanded(groups[groupIndex])
            }
        }
    }

    override fun onGroupCollapsed(
        positionStart: Int,
        itemCount: Int
    ) { //update header
        val headerPosition = positionStart - 1
        notifyItemChanged(headerPosition)
        // only remote if there items to remove
        if (itemCount > 0) {
            notifyItemRangeRemoved(positionStart, itemCount)
            if (expandCollapseListener != null) { //minus one to return the position of the header, not first child
                val groupIndex =
                    expandableList.getUnflattenedPosition(positionStart - 1).groupPos
                expandCollapseListener!!.onGroupCollapsed(groups[groupIndex])
            }
        }
    }

    override fun onGroupClick(flatPos: Int): Boolean {
        if (groupClickListener != null) {
            groupClickListener!!.onGroupClick(flatPos)
        }
        clickEvent(flatPos)
        return expandCollapseController.toggleGroup(flatPos)
    }

    fun toggleGroup(flatPos: Int): Boolean {
        return expandCollapseController.toggleGroup(flatPos)
    }

    fun toggleGroup(group: ExpandableListItem<*, *>?): Boolean {
        return expandCollapseController.toggleGroup(group)
    }

    fun isGroupExpanded(flatPos: Int): Boolean {
        return expandCollapseController.isGroupExpanded(flatPos)
    }

    fun isGroupExpanded(group: ExpandableListItem<*, *>?): Boolean {
        return expandCollapseController.isGroupExpanded(group)
    }

    fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBooleanArray(
            EXPAND_STATE_MAP,
            expandableList.expandedGroupIndexes
        )
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(
                EXPAND_STATE_MAP
            )
        ) {
            return
        }
        expandableList.expandedGroupIndexes =
            savedInstanceState.getBooleanArray(EXPAND_STATE_MAP)!!
        notifyDataSetChanged()
    }

    fun setOnGroupClickListener(listener: OnGroupClickListener?) {
        groupClickListener = listener
    }

    fun setOnGroupExpandCollapseListener(listener: GroupExpandCollapseListener?) {
        expandCollapseListener = listener
    }

    fun setExpandableListItemClickListener(listener: ExpandableListItemClickListener?) {
        this.listener = listener
    }

    fun clickEvent(flatPosition: Int) {
        if (listener != null) {
            val listPos =
                expandableList.getUnflattenedPosition(flatPosition)
            val group = expandableList.getExpandableGroup(listPos)
            when (listPos.type) {
                ExpandableListPosition.GROUP -> listener!!.onGroupItemClick(listPos.groupPos)
                ExpandableListPosition.CHILD -> listener!!.onChildItemClick(
                    listPos.groupPos,
                    listPos.childPos
                )
            }
        }
    }

    companion object {
        private const val EXPAND_STATE_MAP = "expandable_recyclerview_adapter_map"
    }

    init {
        expandableList = ExpandableList(groups)
        expandCollapseController = ExpandCollapseController(expandableList, this)
    }
}