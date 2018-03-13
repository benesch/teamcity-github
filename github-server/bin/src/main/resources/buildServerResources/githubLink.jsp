<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="github_pr_url" type="java.lang.String"--%>
<%--@elvariable id="github_pr_number" type="java.lang.String"--%>
<c:if test="${not empty github_pr_url}">
    <tr>
        <td class="st labels">GitHub PR:</td>
        <td class="st">
            <a href="${util:escapeUrlForQuotes(github_pr_url)}">#${github_pr_number}</a>
        </td>
    </tr>
</c:if>