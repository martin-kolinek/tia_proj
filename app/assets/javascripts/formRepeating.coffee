define([], ->
    addRepeated:(key, addBtn, itemClass, templateClass, removeButtonClass) ->
        renumber = ->
            rgx = new RegExp(key+"""\\[.+\\]""", "g")
            $("."+itemClass).each (index, element) =>
                $(element).find("[name*='"+key+"']").each ->
                    $(this).prop("name", $(this).prop("name").replace(rgx, key+"["+index+"]"))
        remove = (event) ->
            $(this).parents("."+itemClass).remove()
            renumber()
        $("document").ready ->
            $("."+addBtn).click ->
                templ = $(this).parent().find("."+templateClass).first()
                newItem = templ.clone()
                newItem.removeClass(templateClass)
                newItem.removeClass("hidden")
                newItem.addClass(itemClass)
                newItem.find("."+removeButtonClass).on("click", remove)
                templ.before(newItem)
                renumber()
            $("."+removeButtonClass).on("click", remove)
)