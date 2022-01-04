package me.clip.deluxetags.config;

import org.bukkit.configuration.file.FileConfiguration;

public enum Lang {
	CMD_NO_PERMS("cmd.no_permission", "&cYou don't have &7{0} &cto do that!"),
	CMD_TARGET_NOT_ONLINE("cmd.target_not_online", "&f{0} &cis not online!"),
	CMD_NO_TAGS_LOADED("cmd.no_tags_loaded", "&cThere are no tags loaded!"),
	CMD_NO_TAGS_AVAILABLE("cmd.no_tags_available", "&cYou don't have any tags available!"),
	CMD_NO_TAGS_AVAILABLE_TARGET("cmd.no_tags_available_target", "&f{0} &cdon't have any tags available!"),
	CMD_TAG_LIST_FAIL("cmd.tag_list_fail", "&cYou don't have any tags loaded."),
	CMD_TAG_LIST_FAIL_TARGET("cmd.tag_list_fail_target", "&c{0} has no tags loaded."),
	CMD_TAG_LIST("cmd.tags_list", "&f{0} &aavailable tags: &f{1}"),
	CMD_TAG_LIST_ALL("cmd.tags_list_all", "&f{0} &atotal tags loaded: &f{1}"),
	CMD_TAG_LIST_TARGET("cmd.tags_list_others", "&f{0} &ahas &f{1} &atotal tags loaded: &f{2}"),
	CMD_TAG_SEL_INCORRECT("cmd.tag_select_incorrect_usage", "&cIncorrect usage! &7/tags select <tagname>"),
	CMD_TAG_SEL_SUCCESS("cmd.tag_select_success", "&7Your tag was set to: &r{1}"),
	CMD_TAG_SEL_FAIL_INVALID("cmd.tag_select_invalid_name", "&f{0} &cis not a valid tag name!"),
	CMD_TAG_SEL_FAIL_SAMETAG("cmd.tag_select_already_set", "&f{0} &cis already set as your current tag!"),
	CMD_HELP_TITLE("cmd.help_title", "&5&lDeluxeTags &f&oHelp"),
	CMD_HELP_COLOR("cmd.help_color", "&8> &d&l"),
	CMD_HELP_TAGS("cmd.help_tags", "&f&oOpen your tags GUI"),
	CMD_HELP_LIST("cmd.help_list", "&f&oView tags available to you"),
	CMD_HELP_SELECT("cmd.help_select", "&f&oSelect a tag as your active tag"),
	CMD_HELP_ADMIN_SET("cmd.help_admin_set", "&f&oSet a players tag"),
	CMD_HELP_ADMIN_CLEAR("cmd.help_admin_clear", "&f&oClear a players tag"),
	CMD_HELP_ADMIN_CREATE("cmd.help_admin_create", "&f&oCreate a new tag"),
	CMD_HELP_ADMIN_DELETE("cmd.help_admin_delete", "&f&oDelete an existing tag"),
	CMD_HELP_ADMIN_SET_DESC("cmd.help_admin_setdesc", "&f&oSet a description for a tag"),
	CMD_HELP_ADMIN_SET_ORDER("cmd.help_admin_setorder", "&f&oChange the order for a tag"),
	CMD_HELP_ADMIN_SET_DISPLAY("cmd.help_admin_setdisplay", "&f&oChange a tag's display"),
	CMD_HELP_VERSION("cmd.help_version", "&f&oView DeluxeTags version and author information"),
	CMD_HELP_RELOAD("cmd.help_reload", "&f&oReload the tags config"),
	CMD_ADMIN_SET_INCORRECT_ARGS("cmd.admin_set_incorrect_usage", "&cIncorrect usage! &7/tags set <player> <tag>"),
	CMD_ADMIN_SET_NO_TAGS("cmd.admin_set_no_tags_avail", "&f{0} &cdoesn't have any tags available!"),
	CMD_ADMIN_SET_SUCCESS("cmd.admin_set_success", "&f{0}s &atag has been set to: {1} &7({2}&7)"),
	CMD_ADMIN_SET_SUCCESS_TARGET("cmd.admin_set_success_to_target", "&7Your tag has been set to &f{1} &aby &f{2}"),
	CMD_ADMIN_SET_FAIL("cmd.admin_set_success_fail", "&f{0} &cis not a valid tag for &f{1}&c!"),
	CMD_ADMIN_CLEAR_INCORRECT_ARGS("cmd.admin_clear_incorrect_usage", "&cIncorrect usage! &7/tags clear <player>"),
	CMD_ADMIN_CLEAR_NO_TAG_SET("cmd.admin_clear_no_tag_set", "&f{0} &cdoesn't have a tag set!"),
	CMD_ADMIN_CLEAR_SUCCESS("cmd.admin_clear_success", "&f{0}s &atag has been cleared!"),
	CMD_ADMIN_CLEAR_SUCCESS_TARGET("cmd.admin_clear_success_to_target", "&7Your tag has been cleared &aby &f{0}"),
	CMD_ADMIN_CLEAR_FAIL("cmd.admin_set_success_fail", "&f{0} &cis not a valid tag for &f{1}&c!"),
	CMD_ADMIN_CREATE_TAG_INCORRECT("cmd.admin_create_tag_incorrect_usage", "&cIncorrect usage! &7/tags create <identifier> <tag>"),
	CMD_ADMIN_CREATE_TAG_SUCCESS("cmd.admin_create_tag_success", "&aTag created&7: &f{0}&7:&f{1}"),
	CMD_ADMIN_CREATE_TAG_FAIL("cmd.admin_create_tag_fail", "&f{0} &cis already a loaded tag name!"),
	CMD_ADMIN_DELETE_TAG_INCORRECT("cmd.admin_delete_tag_incorrect_usage", "&cIncorrect usage! &7/tags delete <identifier>"),
	CMD_ADMIN_DELETE_TAG_SUCCESS("cmd.admin_delete_tag_success", "&7Tag &f{0} &7has been deleted!"),
	CMD_ADMIN_DELETE_TAG_FAIL("cmd.admin_delete_tag_fail", "&f{0} &cis not a loaded tag name!"),
	CMD_ADMIN_SET_DESCRIPTION_INCORRECT("cmd.admin_set_description_incorrect_usage", "&cIncorrect usage! &7/tags setdesc <identifier> <description>"),
	CMD_ADMIN_SET_DESCRIPTION_SUCCESS("cmd.admin_set_description_success", "{0} &adescription set to &7: &f{2}"),
	CMD_ADMIN_SET_DESCRIPTION_FAIL("cmd.admin_set_description_fail", "&f{0} &cis not a loaded tag name!"),
	CMD_ADMIN_SET_ORDER_INCORRECT("cmd.admin_set_order_incorrect_usage", "&cIncorrect usage! &7/tags setorder <identifier> <order>"),
	CMD_ADMIN_SET_ORDER_SUCCESS("cmd.admin_set_order_success", "&aOrder &f{0}&a set for &7: &f{1}"),
	CMD_ADMIN_SET_ORDER_FAIL("cmd.admin_set_order_fail", "&f{0} &cis not a loaded tag name!"),
	CMD_ADMIN_SET_ORDER_NOT_A_NUMBER("cmd.admin_set_order_not_a_number", "&f{0} &cis not a valid order! It should be a number."),
	CMD_ADMIN_SET_ORDER_ALREADY_EXISTS("cmd.admin_set_order_already_exists", "&cOrder &f{0}&c is already in use."),
	CMD_ADMIN_SET_DISPLAY_INCORRECT("cmd.admin_set_display_incorrect_usage", "&cIncorrect usage! &7/tags setdisplay <identifier> <display>"),
	CMD_ADMIN_SET_DISPLAY_SUCCESS("cmd.admin_set_display_success", "{0} &adisplay set to &7: &f{1}"),
	CMD_ADMIN_SET_DISPLAY_FAIL("cmd.admin_set_display_fail", "&f{0} &cis not a loaded tag name!"),
	CMD_ADMIN_RELOAD("cmd.admin_reload", "&aConfiguration successfully reloaded! &f{0} &atags loaded!"),
	CMD_INCORRECT_USAGE("cmd.incorrect_usage", "&cIncorrect usage! Use &7/tags help"),
	GUI_PLACEHOLDERS_TAG_AVAILABLE("gui.placeholders.tag.available", "&aAvailable"),
	GUI_PLACEHOLDERS_TAG_UNAVAILABLE("gui.placeholders.tag.unavailable", "&cUnavailable"),
	GUI_TAG_SELECTED("gui.tag_selected", "&aYour tag has been set to &f{0} &7({1}&7)"),
	GUI_TAG_DISABLED("gui.tag_disabled", "&7Your tag has been disabled!"), 
	GUI_PAGE_ERROR("gui.page_error", "&cThere was a problem getting the previous page number!")
	;
	

	private final String path, def;
	private static FileConfiguration LANG;

	Lang(final String path, final String start) {
		this.path = path;
		this.def = start;
	}

	public static void setFile(final FileConfiguration config) {
		LANG = config;
	}

	public String getDefault() {
		return this.def;
	}

	public String getPath() {
		return this.path;
	}
	
	public String getConfigValue(final String[] args) {
		String value = this.def;
		
		if (LANG != null) {
			value = LANG.getString(this.path, this.def);
		}

		if (args == null || args.length == 0) {
			return value;
		}

		for (int i = 0; i < args.length; i++) {
			value = value.replace("{" + i + "}", args[i]);
		}

		return value;
	}
}