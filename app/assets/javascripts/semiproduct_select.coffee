define(["jquery", "router", "bootstrap", "dataTables"], ($, router, bs, dt) ->
    selectSemiproduct: (modal, content, idCtl, descCtl) ->
        router.controllers.Semiproducts.list("table").ajax
            error: (xhr, status, err) ->
                alert("error retrieving packs: "+err)
            success: (data, status, xhr) ->
                content.html(data)
                modal.modal("show")
                content.find("table").dataTable()
                content.find("tbody tr").addClass("pointer")
                content.find("tbody tr").click ->
                    desc = $(this).data("description")
                    packid = $(this).data("pack-id")
                    router.controllers.Semiproducts.listSemiproducts(packid, "dropdown").ajax
                        error: (xhr, status, err) ->
                            alert("error retrieving semiproducts: "+err)
                        success: (data, status, xhr) ->
                            inModal = content.find("#sel-list-modal")
                            inContent = content.find("#sel-list-modal-content")
                            inContent.html(data)
                            inContent.find("a").click ->
                                sel = inContent.find("select")
                                serial = sel.find(":selected").text()
                                id = sel.val()
                                idCtl.val(id)
                                descCtl.text(desc + " " + serial)
                                inModal.modal("hide")
                                modal.modal("hide")
                            inModal.modal("show")
)