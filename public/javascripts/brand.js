/**
 * Brand and Models javascript code
 *
 */

function onBrandChange() {
    // add models to model select
    var id = $('#make').val()
    getModelsForBrandLocal(id);
}

function getModelsForBrand(id) {
    //http_url = "@routes.NotificationController.getModelByBrand(31337)";
    http_url = "/getmodelbybrand/31337";
    http_url = http_url.replace("31337", id);

    $("#model").empty();

    $.get( http_url, function( data ) {
        data.forEach( function (ent) {
            $("#model").append(getOption(ent));
        });
    });
}

function getBrands() {
    //http_url = "@routes.NotificationController.getBrands()";
    http_url = "/getbrands";
    $("#model").empty();

    $.get( http_url, function( data ) {
        data.forEach( function (ent) {
            $("#model").append(getOption(ent));
        });
    });
}

function getModelsForBrandLocal(make_id){
    $("#model").empty();
    models.forEach(function (ent) {
        if (ent.make == make_id ) {
            $("#model").append(getOption(ent));
        }
    });
}

function getOption(ent) {
    return "<option value='" + ent.id + "'>" +
        ent.descricao +
        "</option>";
}
