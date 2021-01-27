package com.tech.mvpframework.view.xrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tech.mvpframework.utils.logd

/**
 * 1、上拉刷新
 * 2、下拉刷新
 * 3、空页面
 * 4、抽屉
 */
class XRecyclerView : RecyclerView {

    private val TYPE_REFRESH_HEADER = 10000//下拉刷新的ViewType的码
    private val TYPE_REFRESH_FOOTER = 10001//上拉刷新的ViewType的码

    private var mHeader: IRefreshHeader? = null
    private var mFooter: ILoadingMoreFooter? = null
    private var mEmpty: View? = null
    private val mDataObserver = DataObserver()

    private var mWrapAdapter: WrapAdapter? = null
    private var mLoadingListener: LoadingListener? = null

    /**
     * 标志位
     */
    private var mPullRefreshEnabled = false
    private var mLoadingMoreEnabled = false

    /* ***核心实现***/

    private var mLastY = -1f // 记录拖动的
    private var startY = -1f//記錄開始拖動的Y

    /**
     * 判断是否处于列表头（如果没有下拉刷新的头当作不处于列表头处理）
     *
     * @return
     */
    private val isOnTop: Boolean
        get() = if (mHeader == null) false else mHeader!!.getView().parent != null

    /**
     * 判断是否处于列表尾
     */
    private var isBottom = false

    private var isNoMore = false // 没有更多了

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    private inner class WrapAdapter(val originalAdapter: Adapter<ViewHolder>?) :
        Adapter<ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            if (isHeader(position)) {
                return TYPE_REFRESH_HEADER
            }
            if (isFooter(position)) {
                return TYPE_REFRESH_FOOTER
            }

            val mRealPosition: Int
            if (mPullRefreshEnabled) {
                mRealPosition = position - 1
            } else {
                mRealPosition = position
            }
            if (originalAdapter != null) {
                if (mRealPosition <= originalAdapter.itemCount) {
                    val itemViewType = originalAdapter.getItemViewType(mRealPosition)
                    // 保护机制，防止和头尾布局的ViewType相同
                    if (isReservedItemViewType(itemViewType)) {
                        throw RuntimeException("ViewType中10000和10001已被占用")
                    }
                    return itemViewType
                }
            }
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            if (viewType == TYPE_REFRESH_HEADER) {
                return SimpleViewHolder(mHeader!!.getView())
            } else if (viewType == TYPE_REFRESH_FOOTER) {
                return SimpleViewHolder(mFooter!!.getView())
            }
            return originalAdapter!!.onCreateViewHolder(parent, viewType)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            if (holder is SimpleViewHolder) {
                holder.initOrResetStatus()
            }

            if (isHeader(position)) {
                return
            }

            val mRealPosition: Int
            if (mPullRefreshEnabled) {
                mRealPosition = position - 1
            } else {
                mRealPosition = position
            }
            if (originalAdapter != null) {
                if (mRealPosition < originalAdapter.itemCount) {
                    originalAdapter.onBindViewHolder(holder, mRealPosition)
                }
            }
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
            payloads: List<Any>
        ) {

            if (holder is SimpleViewHolder) {
                holder.initOrResetStatus()
            }

            if (isHeader(position)) {
                return
            }

            val mRealPosition: Int
            if (mPullRefreshEnabled) {
                mRealPosition = position - 1
            } else {
                mRealPosition = position
            }
            if (originalAdapter != null) {
                if (mRealPosition < originalAdapter.itemCount) {
                    if (payloads.isEmpty()) {
                        originalAdapter.onBindViewHolder(holder, mRealPosition)
                    } else {
                        originalAdapter.onBindViewHolder(holder, mRealPosition, payloads)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            var count = 0
            if (mLoadingMoreEnabled) {
                count++
            }
            if (mPullRefreshEnabled) {
                count++
            }
            return if (originalAdapter != null) {
                originalAdapter.itemCount + count
            } else count
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            val manager = recyclerView.layoutManager
            // 设置头部居和尾布局独立占用一行
            if (manager is GridLayoutManager) {
                val gridManager = manager as GridLayoutManager?
                gridManager?.setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (isHeader(position) || isFooter(position))
                            gridManager.getSpanCount()
                        else
                            1
                    }
                })
            }
            originalAdapter?.onAttachedToRecyclerView(recyclerView)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            val lp = holder.itemView.layoutParams
            // 设置头布局和尾布局独立占用一行
            if (lp is StaggeredGridLayoutManager.LayoutParams && (isHeader(holder.layoutPosition) || isFooter(
                    holder.layoutPosition
                ))
            ) {
                lp.isFullSpan = true
            }
            originalAdapter?.onViewAttachedToWindow(holder)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            originalAdapter?.onDetachedFromRecyclerView(recyclerView)
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            originalAdapter?.onViewDetachedFromWindow(holder)
        }

        override fun onViewRecycled(holder: ViewHolder) {
            originalAdapter?.onViewRecycled(holder)
        }

        override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
            return originalAdapter?.onFailedToRecycleView(holder) ?: false
        }

        override fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
            originalAdapter?.unregisterAdapterDataObserver(observer)
        }

        override fun registerAdapterDataObserver(observer: AdapterDataObserver) {
            originalAdapter?.registerAdapterDataObserver(observer)
        }

        fun isFooter(position: Int): Boolean {
            return if (mLoadingMoreEnabled) {
                position == itemCount - 1
            } else {
                false
            }
        }

        fun isHeader(position: Int): Boolean {
            return if (mPullRefreshEnabled) {
                position == 0
            } else {
                false
            }
        }

        private inner class SimpleViewHolder internal constructor(itemView: View) :
            ViewHolder(itemView) {

            fun initOrResetStatus() {
                (itemView as? BaseRefreshHeader)?.resetState()
                (itemView as? BaseLoadingMoreFooter)?.resetState()
            }
        }
    }

    /**
     * 是否保留的adapter的ViewType
     */
    private fun isReservedItemViewType(itemViewType: Int): Boolean {
        return itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_REFRESH_FOOTER
    }

    private var mDeltaY = 0f

    /**
     * 在这里响应下拉事件
     *
     * @param e
     * @return
     */
    override fun onTouchEvent(e: MotionEvent): Boolean {

        // 滑动后收起抽屉
        curSwipeItem?.closeSwipe()

        // 监听为空
        if (mLoadingListener == null) return super.onTouchEvent(e)

        if (mLastY == -1f) {
            mLastY = e.rawY
        }
        if (startY == -1f) {
            startY = e.rawY
        }
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mDeltaY = 0f
                mLastY = e.rawY
                startY = e.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetY = e.rawY - mLastY
                mDeltaY = e.rawY - startY
                logd("aaaa", "startY:${startY},rawY:${e.rawY} mDeltaY:$mDeltaY")
                mLastY = e.rawY
//                logd(
//                    "aaaa",
//                    "move, isBottom;${isBottom} mLoadingMoreEnabled:${mLoadingMoreEnabled} isOnTop:${isOnTop} mPullRefreshEnabled:${mPullRefreshEnabled} mDeltaY:${mDeltaY}"
//                )
                if (isOnTop && mPullRefreshEnabled && mDeltaY > 0) {
                    //符合条件就开始滑动
                    mHeader!!.onMove(offsetY / DRAG_RATE)
                    // 如果头布局出现的情况下就不传递事件给recyclerView
                    if (mHeader!!.getVisibleHeight() > 0) {
                        return false
                    }
                } else if (isBottom && mLoadingMoreEnabled && mDeltaY < 0) {
                    mFooter?.onMove(offsetY / DRAG_RATE)
                    if (mFooter!!.getVisibleHeight() > 0) {
                        scrollBy(0, -(offsetY / DRAG_RATE).toInt())
                        return false
                    }
                }
            }
            else -> {
                logd(
                    "aaaa",
                    "up, isBottom;${isBottom} mLoadingMoreEnabled:${mLoadingMoreEnabled} isOnTop:${isOnTop} mPullRefreshEnabled:${mPullRefreshEnabled} deltaY:${mDeltaY}"
                )
                if (isOnTop && mPullRefreshEnabled && mDeltaY > 0) {
                    if (mHeader!!.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener!!.onRefresh()
                        }
                    }
                } else if (isBottom && mLoadingMoreEnabled && mDeltaY < 0) {
                    scrollTo(0, mFooter!!.getCriticalViewHeight())
                    if (mFooter!!.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener!!.onLoadMore()
                        }
                    }
                }
                mLastY = -1f // reset
                startY = -1f
            }
        }
        return super.onTouchEvent(e)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        // 监听为空
        if (mLoadingListener == null) return

//        if (state == SCROLL_STATE_IDLE
//            && mFooter != null && !mFooter!!.inLoading() && mLoadingMoreEnabled && deltaY < 0
//        ) {
        logd(
            "aaaa",
            "state:${state} deltaY:${mDeltaY}"
        )
        if (mFooter != null && mDeltaY <= 0) {
            val layoutManager = layoutManager//获得相关的布局

            val lastVisibleItemPosition: Int//最后一个可见
            if (layoutManager is GridLayoutManager) {
                lastVisibleItemPosition =
                    layoutManager.findLastVisibleItemPosition()//获得Grid布局最后一个可见的子view的值
            } else if (layoutManager is StaggeredGridLayoutManager) {
                val into = IntArray(layoutManager.spanCount)//获得有多少的跨度就是多少列
                layoutManager.findLastVisibleItemPositions(into)//获得最后一个可见的字view的值
                lastVisibleItemPosition = findMax(into)//获得瀑布流最后一个可见的子view的值
            } else {
                lastVisibleItemPosition =
                    (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()//获得linear布局的最后一个可见的子view的值
            }


            // 满足刷新条件
            val itemCount = mWrapAdapter!!.itemCount - 1
            logd(
                "aaaa",
                "lastVisibleItemPosition:${lastVisibleItemPosition} itemCount:${itemCount}"
            )
            // 是否在尾部
            isBottom = itemCount - lastVisibleItemPosition <= 1
        } else if (mDeltaY > 0) {
            isBottom = false
        }
    }

    /**
     * 用瀑布流里面的获取最大的
     */
    private fun findMax(lastPositions: IntArray): Int {
        var max = lastPositions[0]
        for (value in lastPositions) {
            if (value > max) {
                max = value
            }
        }
        return max
    }

    private inner class DataObserver : AdapterDataObserver() {

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            this.onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            this.onChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            this.onChanged()
        }

        override fun onChanged() {
            mWrapAdapter?.notifyDataSetChanged()
            if (mWrapAdapter?.originalAdapter?.itemCount == 0) {
                visibility = View.GONE
                mEmpty?.visibility = View.VISIBLE
            } else {
                visibility = View.VISIBLE
                mEmpty?.visibility = View.GONE
            }
        }
    }

    /* ****外部get方法****/

    /**
     * 避免用户自己调用getAdapter() 引起的ClassCastException
     */
    override fun getAdapter(): Adapter<*>? {
        return mWrapAdapter?.originalAdapter
    }

    /* ****外部配置方法****/
    override fun setAdapter(adapter: Adapter<ViewHolder>?) {
        if (adapter == null) return
        mWrapAdapter = WrapAdapter(adapter)
        super.setAdapter(mWrapAdapter)
        adapter.registerAdapterDataObserver(mDataObserver)
        mDataObserver.onChanged()
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        if (mWrapAdapter != null) {
            // 设置头部居和尾布局独立占用一行
            if (layout is GridLayoutManager) {
                val gridManager = layout as GridLayoutManager?
                gridManager?.setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (mWrapAdapter!!.isHeader(position) || mWrapAdapter!!.isFooter(
                                position
                            )
                        )
                            gridManager.getSpanCount()
                        else
                            1
                    }
                })
            }
        } else {
            throw RuntimeException("必须在setAdapter()之后调用")
        }
    }

    /**
     * 设置能否上拉刷新
     *
     * @param pullRefreshEnabled
     */
    fun setPullRefreshEnabled(pullRefreshEnabled: Boolean) {
        if (mHeader == null) return
        this.mPullRefreshEnabled = pullRefreshEnabled
    }

    /**
     * 设置头布局
     *
     * @param view
     * @param pullRefreshEnabled  是否使用下拉刷新
     */
    fun setHeaderView(view: BaseRefreshHeader?, pullRefreshEnabled: Boolean) {
        mHeader = view
        setPullRefreshEnabled(pullRefreshEnabled)
    }

    /**
     * 下拉刷新完成
     */
    fun refreshComplete() {
        mHeader?.refreshComplete()
        isNoMore = false // 下拉刷新完成后就可以重置上拉刷新了
        isBottom = false
    }

    /**
     * 可以上啦刷新
     *
     * @param loadingMoreEnabled
     */
    fun setLoadingMoreEnabled(loadingMoreEnabled: Boolean) {
        if (mFooter == null) return
        this.mLoadingMoreEnabled = loadingMoreEnabled
    }

    /**
     * 设置尾布局
     *
     * @param view
     * @param loadingMoreEnabled 是否使用上拉刷新
     */
    fun setFooterView(view: BaseLoadingMoreFooter?, loadingMoreEnabled: Boolean) {
        mFooter = view
        setLoadingMoreEnabled(loadingMoreEnabled)
    }

    /**
     * 加载更多完成
     */
    fun loadingMoreComplete() {
        mFooter?.loadingMoreCompete()
        isBottom = false
    }

    /**
     * 加载更多失败
     */
    fun loadMoreError() {
        mFooter?.loadError()
    }

    /**
     * 没有更多了
     */
    fun setNoMore() {
        isNoMore = true
        mFooter?.loadingNoMore()
    }

    /**
     * 重置没有更多的状态
     */
    fun resetNoMoreState() {
        isNoMore = false
        isBottom = false
        mFooter?.loadingMoreCompete()
    }

    /**
     * 设置空页面(只是拿到外部布局的空页面引用而已，注意！！)
     *
     * @param empty
     */
    fun setEmpty(empty: View) {
        this.mEmpty = empty
        mDataObserver.onChanged()
    }

    fun closeSwipeWithAnim() {
        curSwipeItem?.closeSwipe()
    }

    fun closeSwipeWhitoutAnim() {
        curSwipeItem?.closeSwipe(false)
    }

    /**
     * 引导的回调
     */
    fun setLoadingListener(loadingListener: LoadingListener) {
        this.mLoadingListener = loadingListener
    }

    interface LoadingListener {

        fun onRefresh()

        fun onLoadMore()
    }

    companion object {
        private val DRAG_RATE = 3f  // 下拉的程度和手指移动的程度的比例

        var curSwipeItem: SwipeItem? = null
    }
}
