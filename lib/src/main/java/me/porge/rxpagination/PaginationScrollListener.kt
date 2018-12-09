package me.porge.rxpagination

import android.nfc.tech.MifareUltralight
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * This class represents an scroll listener that requests next page when needed.
 */
class PaginationScrollListener(
    private val perPage: Int
) : RecyclerView.OnScrollListener() {
    private val publishSubject = PublishSubject.create<PaginationCommand>()

    val commandObservable: Observable<PaginationCommand> =
        publishSubject.startWith(PaginationCommand.GetNext(perPage))

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
                && totalItemCount >= MifareUltralight.PAGE_SIZE
            ) {
                publishSubject.onNext(PaginationCommand.GetNext(perPage))
            }
        }
    }
}