package jetbrains.macros;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import jetbrains.macros.base.YouTrackAuthAwareMacroBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtrack.Issue;

import java.util.Map;

import static jetbrains.macros.util.Strings.*;

public class IssueLink extends YouTrackAuthAwareMacroBase {
    private static final Logger LOG = LoggerFactory.getLogger(IssueLink.class);
    private static final String logPrefix = "YTMacro-LinkDebug: ";

    public IssueLink(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate) {
        super(pluginSettingsFactory, transactionTemplate);
    }

    public String execute(Map params, String s, RenderContext renderContext) {
        try {
            final Map<String, Object> context = MacroUtils.defaultVelocityContext();
            final String issueId = (String) params.get(ID);
            String strikeMode = (String) params.get(STRIKE_THRU_PARAM);
            if (strikeMode == null) strikeMode = ID_ONLY;
            String linkTextTemplate = (String) params.get(TEMPLATE_PARAM);
            if (linkTextTemplate == null || linkTextTemplate.isEmpty()) linkTextTemplate = DEFAULT_TEMPLATE;
            String style = (String) params.get(STYLE);
            if (!DETAILED.equals(style)) {
                style = SHORT;
            }

            if (issueId != null && !issueId.isEmpty()) {
                Issue issue = tryGetItem(youTrack.issues, issueId, retries);
                if (issue != null) {
                    setContext(context, strikeMode, linkTextTemplate, issue);
                } else context.put(ERROR, "Issue not found: " + issueId);
            } else {
                context.put(ERROR, "Issue not specified.");
            }
            return VelocityUtils.getRenderedTemplate(BODY_LINK + style + TEMPLATE_EXT, context);
        } catch (Exception ex) {
            LOG.error("YouTrack link macro encounters error", ex);
        }

        return "Issue not specified";
    }

    @Override
    protected String getLoggingPrefix() {
        return logPrefix;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public boolean isInline() {
        return true;
    }

    public boolean hasBody() {
        return false;
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.NO_RENDER;
    }
}