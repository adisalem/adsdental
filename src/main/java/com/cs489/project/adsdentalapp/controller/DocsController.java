package com.cs489.project.adsdentalapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DocsController {

    /**
     * Swagger UI HTML Page
     */
    @GetMapping("/swagger-ui/index.html")
    @ResponseBody
    public String swaggerUiIndex() {
        return getSwaggerUiHtml();
    }

    /**
     * Swagger UI endpoint
     */
    @GetMapping("/swagger-ui")
    @ResponseBody
    public String swaggerUi() {
        return getSwaggerUiHtml();
    }

    private String getSwaggerUiHtml() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Swagger UI</title>\n" +
                "    <meta charset=\"utf-8\"/>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/swagger-ui-dist@3/swagger-ui.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"swagger-ui\"></div>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/swagger-ui-dist@3/swagger-ui-bundle.js\"></script>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/swagger-ui-dist@3/swagger-ui-standalone-preset.js\"></script>\n" +
                "    <script>\n" +
                "    window.onload = function() {\n" +
                "        const ui = SwaggerUIBundle({\n" +
                "            url: '/api-docs',\n" +
                "            dom_id: '#swagger-ui',\n" +
                "            presets: [\n" +
                "                SwaggerUIBundle.presets.apis,\n" +
                "                SwaggerUIStandalonePreset\n" +
                "            ],\n" +
                "            layout: \"StandaloneLayout\",\n" +
                "            onComplete: function() {\n" +
                "                console.log('Swagger UI loaded successfully')\n" +
                "            }\n" +
                "        })\n" +
                "        window.ui = ui\n" +
                "    }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
