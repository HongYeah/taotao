package com.taotao.content.service;

import java.util.List;

import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.pojo.TbContent;

public interface ContentService {

	EasyUIDataGridResult getContentList( long categoryId,int page, int rows);
	TaotaoResult addContent(TbContent content);
	List<TbContent> getContentByCid(long cid);
}
