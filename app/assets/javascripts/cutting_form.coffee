require(["common"], (common) ->
    require(["jquery", "semiproduct_select", "form_repeat"], ($, ss, rep) ->
        handle = rep.addRepeated("part_handle")
    )
)