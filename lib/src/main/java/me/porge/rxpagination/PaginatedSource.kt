package me.porge.rxpagination

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * This class represents a paginated source. This source requests data until receives an empty list.
 */
class PaginatedSource<T>(
    private val requestBlock: (page: Int, perPage: Int) -> Single<List<T>>
) : Consumer<PaginationCommand> {

    private val publishSubject = PublishSubject.create<Page>()
    private val disposeBag = CompositeDisposable()

    private var hasNext: Boolean = INITIAL_HAS_NEXT
    private var currPage: Int = INITIAL_PAGE

    private val observable by lazy {
        publishSubject
            .distinct()
            .flatMapSingle { page ->
                requestBlock(page.number, page.perPage)
                    .subscribeOn(Schedulers.io())
                    .map { page to it }
            }
            .map { (page, items) ->
                currPage = page.number
                hasNext = items.size >= page.perPage

                if (!hasNext) publishSubject.onComplete()

                items
            }
            .doOnDispose { disposeBag.clear() }
    }

    override fun accept(command: PaginationCommand) {
        when (command) {
            is PaginationCommand.GetNext -> {
                if (!hasNext) return
                publishSubject.onNext(Page(currPage + 1, command.perPage))
            }
        }
    }

    /**
     * Gets this source as observable by subscribing a [commandSource].
     */
    fun asObservable(commandSource: Observable<PaginationCommand>): Observable<List<T>> {
        // TODO: Remove the timer and first command emit after observable was subscribed.
        Observable
            .timer(200, TimeUnit.MILLISECONDS)
            .subscribe {
                commandSource
                    .subscribe(this@PaginatedSource)
                    .addTo(disposeBag)
            }
            .addTo(disposeBag)

        return observable
    }

    /**
     * This class represents a page to request.
     */
    private data class Page(
        val number: Int,
        val perPage: Int
    )

    companion object {
        private const val INITIAL_HAS_NEXT = true
        private const val INITIAL_PAGE = 0
    }
}
