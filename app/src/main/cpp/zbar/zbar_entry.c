#include <string.h>
#include "zbar_entry.h"
#include "zbar.h"

char *decodeZbar(int dataWidth, int dataHeight, int left, int top, int width, int height,
                 char *pixbuf) {

    zbar_image_scanner_t *scanner;
    zbar_image_t *zimage;
    zbar_image_t *zgrayimage;

    char *s = NULL;

    zbar_set_verbosity(10);

    zimage = zbar_image_create();
    if (zimage == NULL) {
        return NULL;
    }
    zbar_image_set_format(zimage, *(unsigned long *) "Y800");
    zbar_image_set_size(zimage, dataWidth, dataHeight);
    zbar_image_set_crop(zimage, left, top, width, height);
    zbar_image_set_data(zimage, pixbuf, dataWidth * dataHeight,
                        zbar_image_free_data);

    zgrayimage = zbar_image_convert(zimage, *(unsigned long *) "Y800");
    if (zgrayimage == NULL) {
        return NULL;
    }

    zbar_image_destroy(zimage);

    scanner = zbar_image_scanner_create();
    zbar_image_scanner_set_config(scanner, 0, ZBAR_CFG_ENABLE, 1);
    zbar_scan_image(scanner, zgrayimage);

    const zbar_symbol_t *sym;

    sym = zbar_image_first_symbol(zgrayimage);
    if (sym != NULL) {
        const char *sym_data;
        sym_data = zbar_symbol_get_data(sym);
        return sym_data;
    }
    return NULL;

}

