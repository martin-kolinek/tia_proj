define(["jquery", "bootstrap"], ($, bootstrap) ->
    confirm: (handle, handler) ->
        modal = $("."+handle) 
        modal.find("a").click ->
            modal.modal("hide")
        modal.find("a.btn-danger").click(handler)
        modal.modal("show")
)