package com.taotao.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.EasyUITreeNode;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.content.service.ContentCategoryService;
import com.taotao.mapper.TbContentCategoryMapper;
import com.taotao.pojo.TbContentCategory;
import com.taotao.pojo.TbContentCategoryExample;
import com.taotao.pojo.TbContentCategoryExample.Criteria;

/**
 * 内容分类管理service
 * 
 * @author AdministratorHY
 *
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

	@Autowired
	private TbContentCategoryMapper contentCategoryMapper;

	@Override
	public List<EasyUITreeNode> getContentCategoryList(long parentId) {
		// 根据parentId查询子节点列表
		TbContentCategoryExample example = new TbContentCategoryExample();
		// 设置查询条件
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		// 执行查询
		List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
		List<EasyUITreeNode> resultList = new ArrayList<>();
		for (TbContentCategory tbContentCategory : list) {
			EasyUITreeNode node = new EasyUITreeNode();
			node.setId(tbContentCategory.getId());
			node.setText(tbContentCategory.getName());
			node.setState(tbContentCategory.getIsParent() ? "closed" : "open");
			// 添加到结果列表
			resultList.add(node);
		}
		return resultList;
	}

	@Override
	public TaotaoResult addContentCategory(Long parentId, String name) {
		// 创建一个pojo对象
		TbContentCategory contentCategory = new TbContentCategory();
		// 补全对象的属性
		contentCategory.setParentId(parentId);
		contentCategory.setName(name);
		// 状态。可选值:1(正常),2(删除)
		contentCategory.setStatus(1);
		// 排序，默认为1
		contentCategory.setSortOrder(1);
		contentCategory.setIsParent(false);
		contentCategory.setCreated(new Date());
		contentCategory.setUpdated(new Date());
		// 插入到数据库
		contentCategoryMapper.insert(contentCategory);
		// 判断父节点的状态
		TbContentCategory parent = contentCategoryMapper.selectByPrimaryKey(parentId);
		if (!parent.getIsParent()) {
			// 如果父节点为叶子节点应该改为父节点
			parent.setIsParent(true);
			// 更新父节点
			contentCategoryMapper.updateByPrimaryKey(parent);
		}

		// 返回结果
		return TaotaoResult.ok(contentCategory);
	}

	@Override
	public TaotaoResult updateContentCategory(Long id, String name) {
		TbContentCategory contentCategory = new TbContentCategory();
		contentCategory.setId(id);
		contentCategory.setName(name);
		contentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
		return TaotaoResult.ok();
	}

	/**
	 * 删除（更新status）内容分类 1(正常),2(删除) 1.如果判断是父节点不允许删除
	 */
	@Override
	public TaotaoResult deleteContentCategory(long id) {

		TbContentCategory contentCategory = contentCategoryMapper.selectByPrimaryKey(id);
		
		//此处待优化，下方代码中直接删除父节点迭代删除失效
		if(contentCategory.getIsParent()){
			String msg = "请先删  "+contentCategory.getName()+" 分类下的所有子分类，再删除 "+contentCategory.getName()+"分类";
			TaotaoResult result = TaotaoResult.build(500,msg,null);
			return result;
		}
		//====原文档代码有误，根据父类id查询所有代码传错了============
		// 判断删除的节点是否为父类
		if (contentCategory.getIsParent()) {
			List<TbContentCategory> list = getContentCategoryListByParentId(id);
			// 递归删除
			for (TbContentCategory tbContentCategory : list) {
				deleteContentCategory(contentCategory.getId());
			}
		}
		//=============================================
		
		// 判断父类中是否还有子类节点，没有的话，把父类改成子类
		if (getContentCategoryListByParentId(contentCategory.getParentId()).size() == 1) {
			TbContentCategory parentCategory = contentCategoryMapper.selectByPrimaryKey(contentCategory.getParentId());
			parentCategory.setIsParent(false);
			contentCategoryMapper.updateByPrimaryKey(parentCategory);
		}
		contentCategoryMapper.deleteByPrimaryKey(id);
		return TaotaoResult.ok();
	}

	/**
	 * 获取该节点下的子节点
	 * 
	 * @param id
	 * @return 父节点下的所有孩子节点
	 */
	//====原文档代码有误，根据父类id查询所有代码传错了============
	// 通过父节点id来查询所有子节点，这是抽离出来的公共方法
	private List<TbContentCategory> getContentCategoryListByParentId(long id) {
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(id);
		List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
		return list;
	}

}
