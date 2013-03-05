#require([], ->
    $(document).ready ->
        $("#main_list").dataTable(
            bFilter:false
            aoColumns: [
                null,
                null,
                null,
                null,
                {bSortable:false}
            ]
        )

        $("[data-pack-id]").each ->
            pckid = $(this).data("pack-id")
            $(this).click ->
                semiproductRoutes.controllers.Semiproducts.listSemiproducts(pckid).ajax
                    success : (data, status, xhr) ->
                        $("#sp_content_div").html(data)
                        $("#sp_div").modal("show")
                        $("#semiprod_list").dataTable()
                    error : (xhr, status, err) ->
                        alert("error retrieving data" + status + " " + err)
#)
