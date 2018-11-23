package com.taotao.content.service;

import java.util.List;

import com.taotao.common.pojo.EasyUITreeNode;
import com.taotao.common.pojo.TaotaoResult;

public interface ContentCategoryService {

	List<EasyUITreeNode> getContentCategoryList(long parentId);

	TaotaoResult addContentCategory(Long parentId, String name);

	TaotaoResult updateContentCategory(Long id, String name);
	
	/**
	 * 删除内容分类
	 * @param id
	 * @return
	 */
	TaotaoResult deleteContentCategory(long id);
}
