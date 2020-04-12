package com.tale.service;

import com.tale.model.entity.Options;
import io.github.biezhi.anima.core.AnimaQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.biezhi.anima.Anima.delete;
import static io.github.biezhi.anima.Anima.select;

/**
 * 配置Service
 *
 * @author biezhi
 * @since 1.3.1
 */
@Service("optionsService")
public class OptionsService {

    /**
     * 保存配置
     *
     * @param key   配置key
     * @param value 配置值
     */
    public void saveOption(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            Options options = new Options();
            options.setName(key);

            long count = select().from(Options.class).where(Options::getName, key).count();

            if (count == 0) {
                options = new Options();
                options.setName(key);
                options.setValue(value);
                options.save();
            } else {
                options = new Options();
                options.setValue(value);
                options.updateById(key);
            }
        }
    }

    /**
     * 获取系统配置
     */
    public Map<String, String> getOptions() {
        Map<String, String> options = new HashMap<>();
        AnimaQuery<Options> animaQuery = select().from(Options.class);
        List<Options> optionsList = animaQuery.all();
        if (null != optionsList) {
            optionsList.forEach(option -> options.put(option.getName(), option.getValue()));
        }
        return options;
    }

    public String getOption(String key) {
        Options options = select().from(Options.class).byId(key);
        if (null != options) {
            return options.getValue();
        }
        return null;
    }

    /**
     * 根据key删除配置项
     *
     * @param key 配置key
     */
    public void deleteOption(String key) {
        if (StringUtils.isNotBlank(key)) {
            delete().from(Options.class).where(Options::getName).like(key + "%").execute();
        }
    }
}
