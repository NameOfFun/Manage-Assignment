<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%-- Pager: First, Prev, page numbers, Next, Last. Needs two request attributes: result (a PageDTO) and pageBaseUrl (the list URL with its filters, without page). --%>
<c:set var="pageUrl" value="${pageBaseUrl}${fn:contains(pageBaseUrl, '?') ? '&' : '?'}page="/>
<nav class="pagination" aria-label="Pagination">
    <c:choose>
        <c:when test="${result.hasPrev}">
            <a class="page-btn" href="${pageUrl}1">&laquo; First</a>
            <a class="page-btn" href="${pageUrl}${result.page - 1}" rel="prev">&lsaquo; Prev</a>
        </c:when>
        <c:otherwise>
            <span class="page-btn disabled">&laquo; First</span>
            <span class="page-btn disabled">&lsaquo; Prev</span>
        </c:otherwise>
    </c:choose>

    <c:forEach var="p" items="${result.pageNumbers}">
        <c:choose>
            <c:when test="${p == 0}"><span class="page-btn disabled">…</span></c:when>
            <c:when test="${p == result.page}"><span class="page-btn active" aria-current="page">${p}</span></c:when>
            <c:otherwise><a class="page-btn" href="${pageUrl}${p}">${p}</a></c:otherwise>
        </c:choose>
    </c:forEach>

    <c:choose>
        <c:when test="${result.hasNext}">
            <a class="page-btn" href="${pageUrl}${result.page + 1}" rel="next">Next &rsaquo;</a>
            <a class="page-btn" href="${pageUrl}${result.totalPages}">Last &raquo;</a>
        </c:when>
        <c:otherwise>
            <span class="page-btn disabled">Next &rsaquo;</span>
            <span class="page-btn disabled">Last &raquo;</span>
        </c:otherwise>
    </c:choose>
</nav>
