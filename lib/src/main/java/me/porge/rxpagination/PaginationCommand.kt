package me.porge.rxpagination

/**
 * This class describes a pagination command.
 */
sealed class PaginationCommand {
    /**
     * This class describes the pagination command of requesting the next page.
     */
    class GetNext(val perPage: Int) : PaginationCommand()
}
