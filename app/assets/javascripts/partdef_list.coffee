require(["common"], (common) ->
    require(["jquery", "dataTables", "list_util", "router", "modal_confirm"], ($, dt, lutil, router, conf) ->
        $(document).ready ->
            $("#main_list").dataTable(lutil.listDt(3))
        $(".hide_partdef").click ->
            id = $(this).data("id")
            conf.confirm("confirm-modal", -> 
                router.controllers.PartDefinitions.hidePartDefinition(id).ajax
                    success: (data, status, xhr) ->
                        location.reload()
                    error: (xhr, status, err) ->
                        alert("Error hiding part definitions "+err)
            )
    )
)