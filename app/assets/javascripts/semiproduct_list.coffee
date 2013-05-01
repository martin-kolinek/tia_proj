require(["common"], (common) ->
    require(["jquery", "dataTables", "bootstrap", "router", "list_util"], ($, dt, bs, router, lutil) ->
        $(document).ready ->
            $("#main_list").dataTable(lutil.listDt(5))

            $("[data-pack-id]").each ->
                pckid = $(this).data("pack-id")
                $(this).click ->
                    router.controllers.Semiproducts.listSemiproducts(pckid).ajax
                        success : (data, status, xhr) ->
                            $("#sp_content_div").html(data)
                            $("#sp_div").modal("show")
                            $("#semiprod_list").dataTable()
                        error : (xhr, status, err) ->
                            alert("error retrieving data" + status + " " + err)
    )
)