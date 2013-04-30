require(["jquery.dataTables", "semiproductRoutes", "bootstrap"], ->
    $(document).ready ->
        $("#main_list").dataTable(
            bFilter:false
            bAutoWidth:false
            aoColumns: [
                null,
                null,
                null,
                null,
                {bSortable:false, sWidth:"9em"}
            ]
        )

        $("[data-pack-id]").each ->
            pckid = $(this).data("pack-id")
            $(this).click ->
                semiproductRoutes.controllers.Semiproducts.listSemiproducts(pckid).ajax
                    success : (data, status, xhr) ->
                        $("#sp_div").html(data)
                        $("#sp_div").modal("show")
                    error : (xhr, status, err) ->
                        alert("error retrieving data" + status + " " + err)
)